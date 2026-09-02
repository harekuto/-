package com.voxline.voice.client;

import java.util.concurrent.atomic.AtomicLong;

public final class ClientDiagnostics {
    public static final ClientDiagnostics INSTANCE=new ClientDiagnostics();
    public final AtomicLong encodedFrames=new AtomicLong(),sentFrames=new AtomicLong(),receivedFrames=new AtomicLong(),queueDrops=new AtomicLong(),playbackDrops=new AtomicLong(),fecFrames=new AtomicLong(),plcFrames=new AtomicLong(),lateFrames=new AtomicLong(),decodeErrors=new AtomicLong();
    private ClientDiagnostics(){}
    public void reset(){encodedFrames.set(0);sentFrames.set(0);receivedFrames.set(0);queueDrops.set(0);playbackDrops.set(0);fecFrames.set(0);plcFrames.set(0);lateFrames.set(0);decodeErrors.set(0);}
}
