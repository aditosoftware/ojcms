package de.adito.ojcms.rest.security.util;

import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.adito.ojcms.rest.security.user.OJUser;

import java.time.*;
import java.util.Date;

/**
 * Utility class to create and decode Json Web Tokens for {@link OJUser}.
 *
 * @author Simon Danner, 27.09.2019
 */
public final class JWTUtil
{
  public static final String USER_MAIL_CLAIM = "claim_user_mail";

  //Generate secret within static context -> it is valid per JVM instance -> tokens will expire with server restart
  private static final Algorithm ALGORITHM = Algorithm.HMAC256(RandomString.generate(50));
  private static final JWTVerifier VERIFIER = JWT.require(ALGORITHM).build();

  private JWTUtil()
  {
  }

  /**
   * Creates a JWT for a {@link OJUser}. It will expire after 24 hours.
   *
   * @param pUser the user to create the token for
   * @return the created JWT as string
   */
  public static String createJwtForUser(OJUser pUser)
  {
    final Date expirationDate = Date.from(Instant.now().plus(Duration.ofDays(1)));

    return JWT.create() //
        .withExpiresAt(expirationDate) //
        .withClaim(USER_MAIL_CLAIM, pUser.getValue(OJUser.MAIL)) //
        .sign(ALGORITHM);
  }

  /**
   * Decodes a string based JWT to a {@link DecodedJWT}.
   *
   * @param pJwt the token string to decode
   * @return the decoded JWT
   * @throws JWTVerificationException if the token is invalid
   */
  public static DecodedJWT decodeJwt(String pJwt)
  {
    return VERIFIER.verify(pJwt);
  }
}
