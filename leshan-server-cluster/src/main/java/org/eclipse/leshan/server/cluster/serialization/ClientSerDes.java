/*******************************************************************************
 * Copyright (c) 2016 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.server.cluster.serialization;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.leshan.LinkObject;
import org.eclipse.leshan.core.request.BindingMode;
import org.eclipse.leshan.server.client.Client;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

/**
 * Functions for serialize and deserialize a Client in JSON.
 */
public class ClientSerDes {

    public static JsonObject jSerialize(Client c) {
        JsonObject o = Json.object();
        o.add("regDate", c.getRegistrationDate().getTime());
        o.add("address", c.getAddress().getHostAddress());
        o.add("port", c.getPort());
        o.add("regAddr", c.getRegistrationEndpointAddress().getHostString());
        o.add("regPort", c.getRegistrationEndpointAddress().getPort());
        o.add("lt", c.getLifeTimeInSec());
        if (c.getSmsNumber() != null) {
            o.add("sms", c.getSmsNumber());
        }
        o.add("ver", c.getLwM2mVersion());
        o.add("bnd", c.getBindingMode().name());
        o.add("ep", c.getEndpoint());
        o.add("regId", c.getRegistrationId());

        JsonArray links = new JsonArray();
        for (LinkObject l : c.getObjectLinks()) {
            JsonObject ol = Json.object();
            ol.add("url", l.getUrl());
            JsonObject at = Json.object();
            for (Map.Entry<String, Object> e : l.getAttributes().entrySet()) {
                if (e.getValue() instanceof Integer) {
                    at.add(e.getKey(), (int) e.getValue());
                } else {
                    at.add(e.getKey(), e.getValue().toString());
                }
            }
            ol.add("at", at);
            links.add(ol);
        }
        o.add("objLink", links);
        JsonObject addAttr = Json.object();
        for (Map.Entry<String, String> e : c.getAdditionalRegistrationAttributes().entrySet()) {
            addAttr.add(e.getKey(), e.getValue());
        }
        o.add("addAttr", addAttr);
        o.add("root", c.getRootPath());
        o.add("lastUp", c.getLastUpdate().getTime());
        return o;
    }

    public static String sSerialize(Client c) {
        return jSerialize(c).toString();
    }

    public static byte[] bSerialize(Client c) {
        return jSerialize(c).toString().getBytes();
    }

    public static Client deserialize(JsonObject jObj) {
        Client.Builder b = new Client.Builder(jObj.getString("regId", null), jObj.getString("ep", null),
                new InetSocketAddress(jObj.getString("address", null), jObj.getInt("port", 0)).getAddress(),
                jObj.getInt("port", 0),
                new InetSocketAddress(jObj.getString("regAddr", null), jObj.getInt("regPort", 0)));
        b.bindingMode(BindingMode.valueOf(jObj.getString("bnd", null)));
        b.lastUpdate(new Date(jObj.getLong("lastUp", 0)));
        b.lifeTimeInSec(jObj.getLong("lt", 0));
        b.lwM2mVersion(jObj.getString("ver", "1.0"));
        b.registrationDate(new Date(jObj.getLong("regDate", 0)));
        if (jObj.get("sms") != null) {
            b.smsNumber(jObj.getString("sms", ""));
        }

        JsonArray links = (JsonArray) jObj.get("objLink");
        LinkObject[] linkObjs = new LinkObject[links.size()];
        for (int i = 0; i < links.size(); i++) {
            JsonObject ol = (JsonObject) links.get(i);

            Map<String, Object> attMap = new HashMap<>();
            JsonObject att = (JsonObject) ol.get("at");
            for (String k : att.names()) {
                JsonValue jsonValue = att.get(k);
                if (jsonValue.isNumber()) {
                    attMap.put(k, jsonValue.asInt());
                } else {
                    attMap.put(k, jsonValue.asString());
                }
            }
            LinkObject o = new LinkObject(ol.getString("url", null), attMap);
            linkObjs[i] = o;
        }
        b.objectLinks(linkObjs);
        Map<String, String> addAttr = new HashMap<>();
        JsonObject o = (JsonObject) jObj.get("addAttr");
        for (String k : o.names()) {
            addAttr.put(k, o.getString(k, ""));
        }
        b.additionalRegistrationAttributes(addAttr);

        return b.build();
    }

    public static Client deserialize(byte[] data) {
        return deserialize((JsonObject) Json.parse(new String(data)));
    }
}