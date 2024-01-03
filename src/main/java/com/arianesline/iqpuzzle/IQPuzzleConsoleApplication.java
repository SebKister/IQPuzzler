package com.arianesline.iqpuzzle;

import static com.arianesline.iqpuzzle.Core.*;

public class IQPuzzleConsoleApplication {
    public static void stopCLI() {
        if (distriExecutorService != null) distriExecutorService.shutdown();
    }
    public static void main(String[] args) {
        initStatic();
        System.console().readLine();
        solve();
        stopCLI();
    }
}
