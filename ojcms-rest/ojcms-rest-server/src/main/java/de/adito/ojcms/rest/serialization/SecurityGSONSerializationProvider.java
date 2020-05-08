package de.adito.ojcms.rest.serialization;

import de.adito.ojcms.rest.application.OJSecuredRestApplication;
import de.adito.ojcms.rest.auth.api.RegistrationRequest;
import de.adito.ojcms.rest.security.AuthenticationRestService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.*;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Serialization provider extension to handle the generic registration request in {@link AuthenticationRestService}.
 * Only used for {@link OJSecuredRestApplication}.
 *
 * @param <REGISTRATION_REQUEST> the generic type of the registration request of the application
 * @author Simon Danner, 26.04.2020
 */
public class SecurityGSONSerializationProvider<REGISTRATION_REQUEST extends RegistrationRequest> extends GSONSerializationProvider
{
  private static final String GENERIC_TYPE_NAME = "REGISTRATION_REQUEST";

  private final Class<REGISTRATION_REQUEST> registrationRequestType;

  public SecurityGSONSerializationProvider(Class<REGISTRATION_REQUEST> pRegistrationRequestType)
  {
    registrationRequestType = pRegistrationRequestType;
  }

  @Override
  public Object readFrom(Class<Object> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType,
      MultivaluedMap<String, String> pHttpHeaders, InputStream pEntityStream) throws IOException, WebApplicationException
  {
    if (GENERIC_TYPE_NAME.equals(pGenericType.getTypeName()))
      return super.readFrom((Class) registrationRequestType, pGenericType, pAnnotations, pMediaType, pHttpHeaders, pEntityStream);

    return super.readFrom(pType, pGenericType, pAnnotations, pMediaType, pHttpHeaders, pEntityStream);
  }
}
