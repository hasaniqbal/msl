/**
 * Copyright (c) 2012-2014 Netflix, Inc.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.msl.keyx;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import com.netflix.msl.MslCryptoException;
import com.netflix.msl.MslEncodingException;
import com.netflix.msl.MslError;
import com.netflix.msl.MslException;
import com.netflix.msl.MslKeyExchangeException;
import com.netflix.msl.entityauth.EntityAuthenticationScheme;
import com.netflix.msl.io.MslEncoderException;
import com.netflix.msl.io.MslEncoderFactory;
import com.netflix.msl.io.MslObject;
import com.netflix.msl.test.ExpectedMslException;
import com.netflix.msl.tokens.MasterToken;
import com.netflix.msl.util.MockMslContext;
import com.netflix.msl.util.MslContext;
import com.netflix.msl.util.MslTestUtils;

/**
 * Key response data unit tests.
 * 
 * Successful calls to
 * {@link KeyResponseData#create(com.netflix.msl.util.MslContext, org.json.MslObject)}
 * covered in the individual key response data unit tests.
 * 
 * @author Wesley Miaw <wmiaw@netflix.com>
 */
public class KeyResponseDataTest {
    /** JSON key master token. */
    private static final String KEY_MASTER_TOKEN = "mastertoken";
    /** JSON key key exchange scheme. */
    private static final String KEY_SCHEME = "scheme";
    /** JSON key key request data. */
    private static final String KEY_KEYDATA = "keydata";
    
    @Rule
    public ExpectedMslException thrown = ExpectedMslException.none();
    
    private static MasterToken MASTER_TOKEN;

    @BeforeClass
    public static void setup() throws MslEncodingException, MslCryptoException {
        ctx = new MockMslContext(EntityAuthenticationScheme.PSK, false);
        encoder = ctx.getMslEncoderFactory();
        MASTER_TOKEN = MslTestUtils.getMasterToken(ctx, 1, 1);
    }
    
    @AfterClass
    public static void teardown() {
        MASTER_TOKEN = null;
        encoder = null;
        ctx = null;
    }
    
    @Test
    public void noMasterToken() throws MslException, MslEncodingException, MslCryptoException, MslKeyExchangeException, MslException, MslEncoderException {
        thrown.expect(MslEncodingException.class);
        thrown.expectMslError(MslError.MSL_PARSE_ERROR);

        final MslObject mo = encoder.createObject();
        mo.put(KEY_MASTER_TOKEN + "x", MslTestUtils.toMslObject(encoder, MASTER_TOKEN));
        mo.put(KEY_SCHEME, KeyExchangeScheme.ASYMMETRIC_WRAPPED.name());
        mo.put(KEY_KEYDATA, encoder.createObject());
        KeyResponseData.create(ctx, mo);
    }
    
    @Test
    public void noScheme() throws MslException, MslException, MslEncoderException {
        thrown.expect(MslEncodingException.class);
        thrown.expectMslError(MslError.MSL_PARSE_ERROR);

        final MslObject mo = encoder.createObject();
        mo.put(KEY_MASTER_TOKEN, MslTestUtils.toMslObject(encoder, MASTER_TOKEN));
        mo.put(KEY_SCHEME + "x", KeyExchangeScheme.ASYMMETRIC_WRAPPED.name());
        mo.put(KEY_KEYDATA, encoder.createObject());
        KeyResponseData.create(ctx, mo);
    }
    
    @Test
    public void noKeydata() throws MslException, MslException, MslEncoderException {
        thrown.expect(MslEncodingException.class);
        thrown.expectMslError(MslError.MSL_PARSE_ERROR);

        final MslObject mo = encoder.createObject();
        mo.put(KEY_MASTER_TOKEN, MslTestUtils.toMslObject(encoder, MASTER_TOKEN));
        mo.put(KEY_SCHEME, KeyExchangeScheme.ASYMMETRIC_WRAPPED.name());
        mo.put(KEY_KEYDATA + "x", encoder.createObject());
        KeyResponseData.create(ctx, mo);
    }
    
    @Test
    public void invalidMasterToken() throws MslException, MslEncodingException, MslCryptoException, MslKeyExchangeException, MslException {
        thrown.expect(MslEncodingException.class);
        thrown.expectMslError(MslError.MSL_PARSE_ERROR);

        final MslObject mo = encoder.createObject();
        mo.put(KEY_MASTER_TOKEN + "x", encoder.createObject());
        mo.put(KEY_SCHEME, KeyExchangeScheme.ASYMMETRIC_WRAPPED.name());
        mo.put(KEY_KEYDATA, encoder.createObject());
        KeyResponseData.create(ctx, mo);
    }
    
    @Test
    public void unidentifiedScheme() throws MslException, MslException, MslEncoderException {
        thrown.expect(MslKeyExchangeException.class);
        thrown.expectMslError(MslError.UNIDENTIFIED_KEYX_SCHEME);

        final MslObject mo = encoder.createObject();
        mo.put(KEY_MASTER_TOKEN, MslTestUtils.toMslObject(encoder, MASTER_TOKEN));
        mo.put(KEY_SCHEME, "x");
        mo.put(KEY_KEYDATA, encoder.createObject());
        KeyResponseData.create(ctx, mo);
    }
    
    @Test
    public void keyxFactoryNotFound() throws MslException, MslException, MslEncoderException {
        thrown.expect(MslKeyExchangeException.class);
        thrown.expectMslError(MslError.KEYX_FACTORY_NOT_FOUND);

        final MockMslContext ctx = new MockMslContext(EntityAuthenticationScheme.PSK, false);
        ctx.removeKeyExchangeFactories(KeyExchangeScheme.ASYMMETRIC_WRAPPED);
        final MslObject mo = encoder.createObject();
        mo.put(KEY_MASTER_TOKEN, MslTestUtils.toMslObject(encoder, MASTER_TOKEN));
        mo.put(KEY_SCHEME, KeyExchangeScheme.ASYMMETRIC_WRAPPED.name());
        mo.put(KEY_KEYDATA, encoder.createObject());
        KeyResponseData.create(ctx, mo);
    }
    
    /** MSL context. */
    private static MslContext ctx;
    /** MSL encoder factory. */
    private static MslEncoderFactory encoder;
}
