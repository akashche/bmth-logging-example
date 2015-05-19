Example of Byteman usage for collecting debug logging
-----------------------------------------------------

This project contains Byteman Helper implementation that collects debug logging messages with the following structure:

    {
        "timestamp": 1432026268755,
        "message": "servlet post measurement",
        "stackTrace": "java.lang.Thread.getStackTrace(Thread.java:1589)\ncom.redhat.byteman.thermostat.helper.Helper.log(Helper.java:50)[...]",
        "threadName": "etomcat-3",
        "className": "java.lang.Thread",
        "methodName": "run",
        "state": {
            "time": 1853,
            "compileCount": 5
        }
    }

Frontend module allows to render these messages as SVG charts using JFreeChart library.