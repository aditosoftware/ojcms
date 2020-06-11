package de.adito.ojcms.rest.serialization;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static de.adito.ojcms.rest.auth.util.OJGsonSerializer.GSON_INSTANCE;

/**
 * The de-/serialization provider for REST resources using GSON as library.
 *
 * @author Simon Danner, 03.11.2019
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
      return GSON_INSTANCE.fromJson(reader, pType);
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
      writer.write(GSON_INSTANCE.toJson(pObject));
    }
  }
}
