/**
 * Copyright (C) 2011 BonitaSoft S.A.
 * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ow2.bonita.services.impl;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mostly copied from {@link CookieManager}
 * 
 * Modified: get authenticated user from the headers and return the cookie only if it's the same
 * 
 * 
 * @author Baptiste Mesta
 * 
 */
public class AuthAwareCookieManager extends CookieManager {
  /* ---------------- Fields -------------- */

  private CookiePolicy policyCallback;

  private Map<HttpCookie, List<String>> authMap = new HashMap<HttpCookie, List<String>>();

  private ThreadLocal<List<String>> lastAuth = new ThreadLocal<List<String>>();

  /* ---------------- Ctors -------------- */

  /**
   * Create a new cookie manager with specified cookie store and cookie policy.
   * 
   * @param store
   *          a <tt>CookieStore</tt> to be used by cookie manager. if
   *          <tt>null</tt>, cookie manager will use a default one, which is an
   *          in-memory CookieStore implmentation.
   * @param cookiePolicy
   *          a <tt>CookiePolicy</tt> instance to be used by cookie manager as
   *          policy callback. if <tt>null</tt>, ACCEPT_ORIGINAL_SERVER will be
   *          used.
   */
  public AuthAwareCookieManager(CookieStore store, CookiePolicy cookiePolicy) {
    super(store,cookiePolicy);
    policyCallback = (cookiePolicy == null) ? CookiePolicy.ACCEPT_ORIGINAL_SERVER : cookiePolicy;
  }

  /* ---------------- Public operations -------------- */

  /**
   * To set the cookie policy of this cookie manager.
   * 
   * <p>
   * A instance of <tt>CookieManager</tt> will have cookie policy
   * ACCEPT_ORIGINAL_SERVER by default. Users always can call this method to set
   * another cookie policy.
   * 
   * @param cookiePolicy
   *          the cookie policy. Can be <tt>null</tt>, which has no effects on
   *          current cookie policy.
   */
  public void setCookiePolicy(CookiePolicy cookiePolicy) {
    if (cookiePolicy != null) {
      policyCallback = cookiePolicy;
      super.setCookiePolicy(cookiePolicy);
    }
  }

  public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
    // pre-condition check
    if (uri == null || requestHeaders == null) {
      throw new IllegalArgumentException("Argument is null");
    }

    Map<String, List<String>> cookieMap = new java.util.HashMap<String, List<String>>();
    // if there's no default CookieStore, no way for us to get any cookie
    if (getCookieStore() == null)
      return Collections.unmodifiableMap(cookieMap);

    List<HttpCookie> cookies = new java.util.ArrayList<HttpCookie>();
    List<String> auth = requestHeaders.get(StandardAuthenticationProviderWithUserInHeaders.USER_FOR_COOKIE);
    for (HttpCookie cookie : getCookieStore().get(uri)) {
      // apply path-matches rule (RFC 2965 sec. 3.3.4)
      if (pathMatches(uri.getPath(), cookie.getPath()) && isSameAuth(auth, cookie)) {
        cookies.add(cookie);
      }
    }
    lastAuth.set(auth);

    // apply sort rule (RFC 2965 sec. 3.3.4)
    List<String> cookieHeader = sortByPath(cookies);

    cookieMap.put("Cookie", cookieHeader);
    return Collections.unmodifiableMap(cookieMap);
  }

  /**
   * @param list
   * @param cookie
   * @return
   */
  private boolean isSameAuth(List<String> list, HttpCookie cookie) {
    List<String> cred = authMap.get(cookie);

    return list == null|| list != null && list.equals(cred);
  }

  public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
    // pre-condition check
    if (uri == null || responseHeaders == null) {
      throw new IllegalArgumentException("Argument is null");
    }

    // if there's no default CookieStore, no need to remember any cookie
    if (getCookieStore() == null)
      return;

    for (String headerKey : responseHeaders.keySet()) {
      // RFC 2965 3.2.2, key must be 'Set-Cookie2'
      // we also accept 'Set-Cookie' here for backward compatibility
      if (headerKey == null || !(headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey.equalsIgnoreCase("Set-Cookie"))) {
        continue;
      }

      for (String headerValue : responseHeaders.get(headerKey)) {
        try {
          List<HttpCookie> cookies = HttpCookie.parse(headerValue);
          for (HttpCookie cookie : cookies) {
            if (shouldAcceptInternal(uri, cookie)) {
              getCookieStore().add(uri, cookie);
              authMap.put(cookie, lastAuth.get());
              lastAuth.set(null);
            }
          }
        } catch (IllegalArgumentException e) {
          // invalid set-cookie header string
          // no-op
        }
      }
    }
  }

  /* ---------------- Private operations -------------- */

  // to determine whether or not accept this cookie
  private boolean shouldAcceptInternal(URI uri, HttpCookie cookie) {
    try {
      return policyCallback.shouldAccept(uri, cookie);
    } catch (Exception ignored) { // pretect against malicious callback
      return false;
    }
  }

  /*
   * path-matches algorithm, as defined by RFC 2965
   */
  private boolean pathMatches(String path, String pathToMatchWith) {
    if (path == pathToMatchWith)
      return true;
    if (path == null || pathToMatchWith == null)
      return false;
    if (path.startsWith(pathToMatchWith))
      return true;

    return false;
  }

  /*
   * sort cookies with respect to their path: those with more specific Path
   * attributes precede those with less specific, as defined in RFC 2965 sec.
   * 3.3.4
   */
  private List<String> sortByPath(List<HttpCookie> cookies) {
    Collections.sort(cookies, new CookiePathComparator());

    List<String> cookieHeader = new java.util.ArrayList<String>();
    for (HttpCookie cookie : cookies) {
      // Netscape cookie spec and RFC 2965 have different format of Cookie
      // header; RFC 2965 requires a leading $Version="1" string while Netscape
      // does not.
      // The workaround here is to add a $Version="1" string in advance
      if (cookies.indexOf(cookie) == 0 && cookie.getVersion() > 0) {
        cookieHeader.add("$Version=\"1\"");
      }

      cookieHeader.add(cookie.toString());
    }
    return cookieHeader;
  }

  static class CookiePathComparator implements Comparator<HttpCookie> {
    public int compare(HttpCookie c1, HttpCookie c2) {
      if (c1 == c2)
        return 0;
      if (c1 == null)
        return -1;
      if (c2 == null)
        return 1;

      // path rule only applies to the cookies with same name
      if (!c1.getName().equals(c2.getName()))
        return 0;

      // those with more specific Path attributes precede those with less
      // specific
      if (c1.getPath().startsWith(c2.getPath()))
        return -1;
      else if (c2.getPath().startsWith(c1.getPath()))
        return 1;
      else
        return 0;
    }
  }
}
