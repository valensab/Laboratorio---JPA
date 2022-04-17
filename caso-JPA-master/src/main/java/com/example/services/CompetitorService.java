/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.services;

import com.example.PersistenceManager;
import com.example.models.Competitor;
import com.example.models.CompetitorDTO;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Mauricio
 */
@Path("/competitors")
@Produces(MediaType.APPLICATION_JSON)
public class CompetitorService {

    @PersistenceContext(unitName = "CompetitorsPU")
    EntityManager entityManager;

    @PostConstruct
    public void init() {
        try {
            entityManager = PersistenceManager.getInstance().getEntityManagerFactory().createEntityManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/get")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        Query q = entityManager.createQuery("select u from Competitor u order by u.surname ASC");
        List<Competitor> competitors = q.getResultList();
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(competitors).build();
    }

    @POST
    @Path("/add")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCompetitor(CompetitorDTO competitor) {
        JSONObject rta = new JSONObject();
        Competitor competitorTmp = new Competitor();
        competitorTmp.setAddress(competitor.getAddress());
        competitorTmp.setAge(competitor.getAge());
        competitorTmp.setCellphone(competitor.getCellphone());
        competitorTmp.setCity(competitor.getCity());
        competitorTmp.setCountry(competitor.getCountry());
        competitorTmp.setName(competitor.getName());
        competitorTmp.setSurname(competitor.getSurname());
        competitorTmp.setTelephone(competitor.getTelephone());
        competitorTmp.setPassword(competitor.getPassword());

        try {
            entityManager.getTransaction().begin();
            entityManager.persist(competitorTmp);
            entityManager.getTransaction().commit();
            entityManager.refresh(competitorTmp);
            rta.put("competitor_id", competitorTmp.getId());
        } catch (Throwable t) {
            t.printStackTrace();
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            competitorTmp = null;
        } finally {
            entityManager.clear();
            entityManager.close();
        }
        return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(rta).build();
    }

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)

    public Response loginCompetitor(Competitor competitor) throws JSONException {
        JSONObject rta = new JSONObject();
        String message;
        try {
            entityManager.getTransaction().begin();
            Query q = entityManager.createQuery("SELECT u.address, u.password FROM Competitor u WHERE (u.address = ?1) AND (u.password = ?2)");
            q.setParameter(1, competitor.getAddress());
            q.setParameter(2, competitor.getPassword());
            System.out.println(competitor.getAddress());
            System.out.println(competitor.getPassword());
            System.out.println(q);
            List result = q.getResultList();
            System.out.println(result);

            if (!result.isEmpty()) {
                rta.put("competitor_address", competitor.getAddress());
                rta.put("competitor_password", competitor.getPassword());
                rta.put("message", "Successful login");
                return Response.status(200).header("Access-Control-Allow-Origin", "*").entity(rta).build();
            } else {
                rta.put("message", "The address or password is incorrect");
                return Response.status(BAD_REQUEST).entity(rta).build();
            }

        } catch (NoResultException e) {
            message = e.getMessage();
            return Response.status(401).entity(message).build();
        } finally {
            entityManager.close();
        }

    }
}
