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
  public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
  {
    return true;
  }

  @Override
  public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException
  {
    try (InputStreamReader reader = new InputStreamReader(entityStream))
    {
      return GSON.fromJson(reader, type);
    }
  }

  @Override
  public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
  {
    return true;
  }

  @Override
  public void writeTo(Object pObject, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
      MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws WebApplicationException
  {
    try (PrintWriter writer = new PrintWriter(entityStream))
    {
      writer.write(GSON.toJson(pObject));
    }
  }
}
