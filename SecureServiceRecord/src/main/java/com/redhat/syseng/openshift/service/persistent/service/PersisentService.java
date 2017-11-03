package com.redhat.syseng.openshift.service.persistent.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import com.redhat.syseng.openshift.service.persistent.model.Error;
import com.redhat.syseng.openshift.service.persistent.model.Inventory;
import com.redhat.syseng.openshift.service.persistent.model.Keyword;
import com.redhat.syseng.openshift.service.persistent.model.ProvisionRecord;
import com.redhat.syseng.openshift.service.persistent.utils.Utils;

@Path("/")
@Stateless
@LocalBean
public class PersisentService
{

	@PersistenceContext
	private EntityManager em;

	private Logger logger = Logger.getLogger( getClass().getName() );

	@Path("/records")
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ProvisionRecord addRecord(ProvisionRecord record)
	{
		try
		{
			logInfo("Will persist this record " + record );
			em.persist(record );
			return record;
		}
		catch( RuntimeException e )
		{
			logError( "Got exception " + e.getMessage() );
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/records")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Collection<ProvisionRecord> getRecords(@Context UriInfo uriInfo)
	{
		try
		{
			MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
			if( queryParams.containsKey( "featured" ) )
			{
				return em.createNamedQuery("Record.findFeatured", ProvisionRecord.class ).getResultList();
			}
			else if( queryParams.containsKey( "keyword" ) )
			{
				Collection<ProvisionRecord> records = new HashSet<ProvisionRecord>();
				for( String keyword : queryParams.get( "keyword" ) )
				{
					try
					{
						TypedQuery<Keyword> query = em.createNamedQuery( "Keyword.findKeyword", Keyword.class );
						query.setParameter( "query", keyword );
						Keyword keywordEntity = query.getSingleResult();
						List<ProvisionRecord> keywordRecords = keywordEntity.getRecords();
						logInfo( "Found " + keyword + ": " + keywordRecords );
						records.addAll( keywordRecords );
					}
					catch( NoResultException e )
					{
						//keyword not found, which is acceptable
					}
				}
				return records;
			}
			else
			{
				throw new Error( HttpURLConnection.HTTP_BAD_REQUEST, "All records cannot be returned" ).asException();
			}
		}
		catch( WebApplicationException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@GET
	@Path("/records/{sku}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ProvisionRecord getRecord(@PathParam("sku") Long sku)
	{
		try
		{
			logInfo( "SKU is " + sku );
			ProvisionRecord record = em.find(ProvisionRecord.class, sku );
			if( record == null )
			{
				throw new Error( HttpURLConnection.HTTP_NOT_FOUND, "Record not found" ).asException();
			}
			return record;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PUT
	@Path("/records/{sku}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ProvisionRecord updateRecord(@PathParam("sku") Long sku, ProvisionRecord record)
	{
		ProvisionRecord entity = getRecord( sku );
		try
		{
			//Ignore any attempt to update record SKU:
			record.setSku( sku );
			Utils.copy( record, entity, false );
			em.merge( entity );
			return record;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@PATCH
	@Path("/records/{sku}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public ProvisionRecord partiallyUpdateRecord(@PathParam("sku") Long sku, ProvisionRecord record)
	{
		ProvisionRecord entity = getRecord( sku );
		try
		{
			//Ignore any attempt to update record SKU:
			record.setSku( sku );
			Utils.copy( record, entity, true );
			em.merge( entity );
			return record;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@DELETE
	@Path("/records/{sku}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void deleteRecord(@PathParam("sku") Long sku)
	{
		ProvisionRecord record = getRecord( sku );
		try
		{
			em.remove( record );
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@Path("/keywords")
	@POST
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Keyword addKeyword(Keyword keyword)
	{
		try
		{
			logInfo( "Will persist keyword " + keyword );
			em.persist( keyword );
			return keyword;
		}
		catch( RuntimeException e )
		{
			logError( "Got exception " + e.getMessage() );
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@POST
	@Path("/classify/{sku}")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void classifyRecord(@PathParam("sku") Long sku, List<Keyword> keywords)
	{
		ProvisionRecord record = getRecord( sku );
		logInfo( "Asked to classify " + record + " as " + keywords );
		try
		{
			record.setKeywords( keywords );
			em.merge( record );
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@POST
	@Path("/reduce/")
	@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public void reduceInventory(Inventory[] inventoryAdjustment)
	{
		try
		{
			logInfo( "Asked to reduce inventory: " + Arrays.toString( inventoryAdjustment ) );
			for( Inventory inventory : inventoryAdjustment )
			{
				ProvisionRecord record = em.find(ProvisionRecord.class, inventory.getSku(), LockModeType.PESSIMISTIC_WRITE );
				logInfo( "Looked up record as " + record );
				if( record == null )
				{
					throw new Error( HttpURLConnection.HTTP_NOT_FOUND, "Record not found" ).asException();
				}
				int availability = record.getAvailability();
				if( inventory.getQuantity() > availability )
				{
					String message = "Insufficient availability for " + inventory.getSku();
					throw new Error( HttpURLConnection.HTTP_CONFLICT, message ).asException();
				}
				else
				{
					record.setAvailability( availability - inventory.getQuantity() );
					em.merge( record );
					logInfo( "Saved " + record );
				}
			}
		}
		catch( WebApplicationException e )
		{
			throw e;
		}
		catch( RuntimeException e )
		{
			throw new Error( HttpURLConnection.HTTP_INTERNAL_ERROR, e ).asException();
		}
	}

	@Target({ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@HttpMethod("PATCH")
	public @interface PATCH
	{
	}

	private void logInfo(String message)
	{
		logger.log( Level.INFO, message );
	}

	private void logError(String message)
	{
		logger.log( Level.SEVERE, message );
	}
}