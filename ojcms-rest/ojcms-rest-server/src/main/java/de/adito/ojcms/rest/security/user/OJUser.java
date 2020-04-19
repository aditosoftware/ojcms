package de.adito.ojcms.rest.security.user;

import de.adito.ojcms.beans.*;
import de.adito.ojcms.beans.annotations.*;
import de.adito.ojcms.beans.literals.fields.types.*;
import de.adito.ojcms.rest.application.OJRestApplication;
import de.adito.ojcms.rest.security.user.exceptions.BadRestoreCodeException;
import de.adito.ojcms.rest.security.util.RandomString;

import java.time.*;
import java.util.Objects;

import static de.adito.ojcms.rest.auth.util.SharedUtils.*;

/**
 * Base class for users of the application.
 * Users are part of the security concept of REST applications.
 * <p>
 * The id of an user is its mail address. They will be authenticated by password. All other REST requests use Json Web Tokens for
 * authentication. This user class also provides properties for an authentication restore mechanism.
 * <p>
 * The concrete user type of the application has to be defined in {@link OJRestApplication}.
 *
 * @author Simon Danner, 20.09.2019
 */
public abstract class OJUser extends OJBean
{
  @Identifier
  @FinalNeverNull
  public static final TextField MAIL = OJFields.create(OJUser.class);
  @FinalNeverNull
  public static final TextField DISPLAY_NAME = OJFields.create(OJUser.class);
  @NeverNull
  public static final TextField PASSWORD = OJFields.create(OJUser.class);
  public static final TimestampField RESTORE_TIMESTAMP = OJFields.create(OJUser.class);
  @OptionalField
  public static final TextField RESTORE_CODE =
      OJFields.createOptional(OJUser.class, (pUser, pCode) -> pUser.getValue(RESTORE_TIMESTAMP) != null);

  /**
   * Initializes the user with its mail address and a display name.
   * An initial password will be generated.
   *
   * @param pMailAddress the mail address of the new user
   * @param pDisplayName the display name of the new user
   */
  protected OJUser(String pMailAddress, String pDisplayName)
  {
    setValue(MAIL, pMailAddress);
    setValue(DISPLAY_NAME, pDisplayName);
    generateNewPassword();
  }

  /**
   * Required by OJCMS-persistence.
   */
  @SuppressWarnings("unused")
  protected OJUser()
  {
  }

  /**
   * Generates a new random password for this user.
   *
   * @return the new password
   */
  public String generateNewPassword()
  {
    final String newPassword = RandomString.generate(50);
    setValue(PASSWORD, newPassword);
    return newPassword;
  }

  /**
   * Generates a new restore code for this user.
   */
  public void generateRestoreCode()
  {
    setValue(RESTORE_TIMESTAMP, Instant.now());
    setValue(RESTORE_CODE, RandomString.generate(CODE_LENGTH));
  }

  /**
   * Validates a given restore code for this user. This method can only be used if a code has been generated before.
   * If the code is valid and has not been expired, it will be reset.
   *
   * @param pRestoreCode the restore code to validate for this user
   * @throws BadRestoreCodeException if the code does not match or has expired
   */
  public void validateAndResetRestoreCode(String pRestoreCode) throws BadRestoreCodeException
  {
    final String code = getValue(RESTORE_CODE);

    if (code == null)
      throw new IllegalStateException("No restore code set!");

    if (!Objects.equals(pRestoreCode, code))
      throw new BadRestoreCodeException(pRestoreCode);

    final Instant restoreCodeTimestamp = getValue(RESTORE_TIMESTAMP);
    if (Duration.between(restoreCodeTimestamp, Instant.now()).compareTo(RESTORE_CODE_EXPIRATION_THRESHOLD) > 0)
      throw new BadRestoreCodeException(RESTORE_CODE_EXPIRATION_THRESHOLD);

    setValue(RESTORE_CODE, null);
    setValue(RESTORE_TIMESTAMP, null);
  }
}
