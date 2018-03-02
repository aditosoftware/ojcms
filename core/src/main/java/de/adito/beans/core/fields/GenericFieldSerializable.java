package de.adito.beans.core.fields;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * A bean field that holds any generic type value.
 * The value has to be serializable here.
 * For non serializable values use {@link GenericField}
 *
 * @param <TYPE> the generic data type this field is referring to
 * @author Simon Danner, 20.02.2019
 */
public class GenericFieldSerializable<TYPE extends Serializable> extends GenericField<TYPE> implements ISerializableField<TYPE>
{
  public GenericFieldSerializable(@NotNull Class<TYPE> pType, @NotNull String pName, @NotNull Collection<Annotation> pAnnotations)
  {
    super(_checkGenericType(pType), pName, pAnnotations);
  }

  @Override
  public String toPersistent(TYPE pValue)
  {
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutputStream os = new ObjectOutputStream(bos))
    {
      os.writeObject(pValue);
      return new String(bos.toByteArray());
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public TYPE fromPersistent(String pSerialString)
  {
    try (ObjectInputStream is = new ObjectInputStream(new ByteArrayInputStream(pSerialString.getBytes())))
    {
      //noinspection unchecked
      return (TYPE) is.readObject();
    }
    catch (IOException | ClassNotFoundException pE)
    {
      throw new RuntimeException(pE);
    }
  }
}
