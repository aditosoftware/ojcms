package de.adito.ojcms.rest.serialization;

import de.adito.ojcms.rest.auth.util.GSONFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static de.adito.ojcms.rest.auth.util.GSONFactory.GSON;

/**
 * The de-/serialization provider for REST resources using GSON as library.
 * The GSON instance also provides a mechanism to serialize only getter interface types.
 *
 * @author Simon Danner, 03.11.2019
 * @see GSONFactory
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GSONSerializationProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{
  @Override
  public boolean isReadable(Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType)
  {
    return true;
  }

  @Override
  public Object readFrom(Class<Object> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType,
      MultivaluedMap<String, String> pHttpHeaders, InputStream pEntityStream) throws IOException, WebApplicationException
  {
    try (InputStreamReader reader = new InputStreamReader(pEntityStream))
    {
      return GSON.fromJson(reader, pType);
    }
  }

  @Override
  public boolean isWriteable(Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType)
  {
    return true;
  }

  @Override
  public void writeTo(Object pObject, Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType,
      MultivaluedMap<String, Object> pHttpHeaders, OutputStream pEntityStream) throws WebApplicationException
  {
    try (PrintWriter writer = new PrintWriter(pEntityStream))
    {
      writer.write(GSON.toJson(pObject));
    }
  }
}
