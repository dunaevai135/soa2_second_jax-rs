package com.example.soa2_3;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.github.underscore.lodash.U;

import javax.ws.rs.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Path("/price")
public class DiscountMakerResource {
    private static final Client client = ClientBuilder.newBuilder().build();

    @POST
    @Path("/increase/{pct}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response increase(@PathParam("pct") int pct) {
        String rest_uri = System.getenv("SOA_1ST_SERV_URL");
        if (rest_uri == null || rest_uri.equals(""))
            rest_uri = "https://localhost:8642/";


        Map<String, Object> m = U.fromXmlMap(client.target(rest_uri).request().get().readEntity(String.class));
        Map<Long, Long> prises = new HashMap<>();
        try {
            for (Object e : (ArrayList) m.get("value")) {
                Map a = ((Map)e);
//                System.out.print(a.get("id").getClass() + ": ");
//                System.out.println(a.get("price").getClass());
                prises.put((Long) a.get("id"),
                        Long.parseLong((String) a.get("price")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).build();
        }

        for (Long s: prises.keySet()) {
            long i = (long)(prises.get(s)*(1+((double)pct)/100));
//            System.out.println(i);
            Response r = client.target(rest_uri + s).request()
                    .method("PATCH", Entity.entity(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><price>"+
                                    i
                                    +"</price></root>", MediaType.APPLICATION_XML));

//            System.out.println(r.readEntity(String.class));
            if (r.getStatus() != 200) {
                return r;
            }
        }
        return Response.ok().build();
    }

    @POST
    @Path("/decrease/{pct}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response decrease(@PathParam("pct") int pct) {
        return increase(-pct);
    }
}
