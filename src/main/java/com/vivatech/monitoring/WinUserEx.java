package com.vivatech.monitoring;

import com.sun.jna.platform.win32.WinUser;

public interface WinUserEx extends WinUser {
    int EVENT_SYSTEM_FOREGROUND = 3;
}
