package com.comphenix.protocol.scheduler;

import com.comphenix.protocol.reflect.accessors.MethodAccessor;

public class FoliaTask implements Task {
    private final MethodAccessor cancel;
    private final Object taskHandle;

    public FoliaTask(MethodAccessor cancel, Object taskHandle) {
        this.cancel = cancel;
        this.taskHandle = taskHandle;
    }

    @Override
    public void cancel() {
        cancel.invoke(taskHandle);
    }
}
