package de.adito.ojcms.rest.security;

import de.adito.ojcms.rest.config.RestoreMailConfig;
import de.adito.ojcms.rest.security.user.OJUser;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.MailerBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.logging.*;

import static de.adito.ojcms.rest.auth.util.SharedUtils.RESTORE_CODE_EXPIRATION_THRESHOLD;

/**
 * Responsible to send mails to restore authentication for an user.
 *
 * @author Simon Danner, 23.11.2019
 */
@ApplicationScoped
class RestoreCodeMailSender
{
  private static final Logger LOGGER = Logger.getLogger(RestoreCodeMailSender.class.getName());

  @Inject
  private RestoreMailConfig config;

  /**
   * Sends a mail to an user that contains a string based code to restore its authentication.
   *
   * @param pUser the user to send the mail for
   */
  void sendRestoreCodeMail(OJUser pUser)
  {
    final String mail = pUser.getValue(OJUser.MAIL);
    final String displayName = pUser.getValue(OJUser.DISPLAY_NAME);
    final String restoreCode = pUser.getValue(OJUser.RESTORE_CODE);

    final String text = "Hello " + displayName + "! \n\nYour account restore code is: " + restoreCode + "\nIt will expire in " + //
        RESTORE_CODE_EXPIRATION_THRESHOLD.toMinutes() + " minutes!\n\n" + config.getMailSender();

    final Email email = EmailBuilder.startingBlank() //
        .from(config.getMailSender(), config.getMailUser()) //
        .to(displayName, mail) //
        .withSubject("Account recovery code") //
        .withPlainText(text) //
        .buildEmail();

    try
    {
      MailerBuilder.withSMTPServer(config.getMailHost(), config.getMailPort(), config.getMailUser(), config.getMailPassword()) //
          .buildMailer() //
          .sendMail(email);
    }
    catch (Exception pE)
    {
      LOGGER.log(Level.WARNING, "Mail not sent! mail: " + mail + ", reason: " + pE.getMessage());
    }
  }
}
