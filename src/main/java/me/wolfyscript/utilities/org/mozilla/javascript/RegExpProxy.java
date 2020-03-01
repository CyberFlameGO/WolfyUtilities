/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package me.wolfyscript.utilities.org.mozilla.javascript;

/**
 * A proxy for the regexp package, so that the regexp package can be
 * loaded optionally.
 *
 * @author Norris Boyd
 */
public interface RegExpProxy {
    // Types of regexp actions
    int RA_MATCH = 1;
    int RA_REPLACE = 2;
    int RA_SEARCH = 3;

    boolean isRegExp(Scriptable obj);

    Object compileRegExp(Context cx, String source, String flags);

    Scriptable wrapRegExp(Context cx, Scriptable scope,
                          Object compiled);

    Object action(Context cx, Scriptable scope,
                  Scriptable thisObj, Object[] args,
                  int actionType);

    int find_split(Context cx, Scriptable scope, String target,
                   String separator, Scriptable re,
                   int[] ip, int[] matchlen,
                   boolean[] matched, String[][] parensp);

    Object js_split(Context _cx, Scriptable _scope,
                    String thisString, Object[] _args);
}
