package de.adito.ojcms.rest.config;

import de.adito.ojcms.utils.config.AbstractFileBasedConfig;

import javax.enterprise.context.ApplicationScoped;

/**
 * File based configuration for the mail provider to send user restore codes.
 *
 * @author Simon Danner, 30.11.2019
 */
@ApplicationScoped
public class RestoreMailConfig extends AbstractFileBasedConfig
{
  private static final String CONFIG_PATH = "ojcms_restore_mail.properties";
  private static final String KEY_MAIL_HOST = "MAIL_HOST";
  private static final String KEY_MAIL_PORT = "MAIL_PORT";
  private static final String KEY_MAIL_USER = "MAIL_USER";
  private static final String KEY_MAIL_PASSWORD = "MAIL_PASSWORD";
  private static final String KEY_MAIL_SENDER = "MAIL_SENDER";

  public RestoreMailConfig()
  {
    super(CONFIG_PATH);
  }

  /**
   * The host of the mail provider to use.
   */
  public String getMailHost()
  {
    return readMandatoryProperty(KEY_MAIL_HOST);
  }

  /**
   * The port of the mail provider to use.
   */
  public int getMailPort()
  {
    return readMandatoryIntProperty(KEY_MAIL_PORT);
  }

  /**
   * The name of the user from the mail provider to use.
   */
  public String getMailUser()
  {
    return readMandatoryProperty(KEY_MAIL_USER);
  }

  /**
   * The password of the user from the mail provider to use.
   */
  public String getMailPassword()
  {
    return readMandatoryProperty(KEY_MAIL_PASSWORD);
  }

  /**
   * The name of the sender of the restore code.
   */
  public String getMailSender()
  {
    return readMandatoryProperty(KEY_MAIL_SENDER);
  }
}
