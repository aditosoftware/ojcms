package de.adito.ojcms.rest.serialization;

import de.adito.ojcms.beans.IBean;
import de.adito.ojcms.beans.exceptions.bean.BeanSerializationException;
import de.adito.ojcms.beans.literals.fields.serialization.ISerializableField;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Map;

import static de.adito.ojcms.rest.auth.util.GSONFactory.GSON;
import static java.util.stream.Collectors.toMap;

/**
 * JSON serialization provider for {@link IBean} instances. Only possible for beans that contain {@link ISerializableField} only.
 *
 * @author Simon Danner, 21.04.2020
 */
@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BeanSerializationProvider implements MessageBodyReader<IBean>, MessageBodyWriter<IBean>
{
  @Override
  public boolean isReadable(Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType)
  {
    return true;
  }

  @Override
  public IBean readFrom(Class<IBean> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType,
      MultivaluedMap<String, String> pHttpHeaders, InputStream pEntityStream) throws IOException, WebApplicationException
  {
    try (InputStreamReader reader = new InputStreamReader(pEntityStream))
    {
      final ParameterizedType mapType = ParameterizedTypeImpl.make(Map.class, new Type[]{String.class, Serializable.class}, IBean.class);
      final Map<String, Serializable> serialBeanValues = GSON.fromJson(reader, mapType);

      try
      {
        final IBean bean = createEmptyBeanInstance(pType);
        for (Map.Entry<String, Serializable> entry : serialBeanValues.entrySet())
        {
          final ISerializableField field = (ISerializableField) bean.getFieldByName(entry.getKey());
          //noinspection unchecked
          bean.setValue(field, field.fromPersistent(entry.getValue()));
        }

        return bean;
      }
      catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException pE)
      {
        throw new BeanSerializationException("Provide a default constructor for " + pType.getName() + "! May be private!", pE);
      }
    }
  }

  @Override
  public boolean isWriteable(Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType)
  {
    return true;
  }

  @Override
  public void writeTo(IBean pBean, Class<?> pType, Type pGenericType, Annotation[] pAnnotations, MediaType pMediaType,
      MultivaluedMap<String, Object> pHttpHeaders, OutputStream pEntityStream) throws IOException, WebApplicationException
  {
    if (pBean.streamFields().allMatch(pField -> pField instanceof ISerializableField))
      throw new BeanSerializationException("Bean of type " + pType.getName() + " has non serializable fields!");

    //noinspection unchecked
    final Map<String, Serializable> serialBeanValues = pBean.stream() //
        .collect(toMap(pTuple -> pTuple.getField().getName(),
            pTuple -> ((ISerializableField) pTuple.getField()).toPersistent(pTuple.getValue())));

    try (PrintWriter writer = new PrintWriter(pEntityStream))
    {
      writer.write(GSON.toJson(serialBeanValues));
    }
  }

  /**
   * Creates an empty bean instance by calling its default constructor.
   *
   * @param pBeanType the bean type to instantiate
   * @return the create bean instance
   */
  private static IBean createEmptyBeanInstance(Class<IBean> pBeanType)
      throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException
  {
    final Constructor<IBean> defaultConstructor = pBeanType.getDeclaredConstructor();
    if (!defaultConstructor.isAccessible())
      defaultConstructor.setAccessible(true);

    return defaultConstructor.newInstance();
  }
}
