/*
 * Copyright 2015 Real Logic Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.real_logic.fix_gateway.session;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static uk.co.real_logic.fix_gateway.session.SessionState.*;

public class AcceptorSessionTest extends AbstractSessionTest
{
    private AcceptorSession session = new AcceptorSession(
        HEARTBEAT_INTERVAL, CONNECTION_ID, fakeClock, mockProxy, mockPublication, null);

    @Test
    public void shouldInitiallyBeConnected()
    {
        assertEquals(CONNECTED, session.state());
    }

    @Test
    public void shouldBeActivatedBySuccessfulLogin()
    {
        onLogon(1);

        verify(mockProxy).logon(HEARTBEAT_INTERVAL, 1);
        verifyNoFurtherMessages();
        assertState(ACTIVE);
    }

    @Test
    public void shouldRequestResendIfHighSeqNoLogon()
    {
        onLogon(3);

        verify(mockProxy).resendRequest(1, 1, 2);
        verifyNoFurtherMessages();
        assertState(AWAITING_RESEND);
    }

    @Test
    public void shouldLogoutIfLowSeqNoLogon()
    {
        session.lastReceivedMsgSeqNum(2);

        onLogon(1);
        verifyDisconnect();
        verifyNoFurtherMessages();
    }

    @Test
    public void shouldDisconnectIfFirstMessageNotALogon()
    {
        session.onMessage(1);

        verifyDisconnect();
        verifyNoFurtherMessages();
    }

    protected Session session()
    {
        return session;
    }
}
