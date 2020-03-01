package com.tuvakov.zeyoube.android.data;

import java.util.Objects;

public class SyncStatus {
    private int resultCode;
    private Exception exception;

    public SyncStatus(int resultCode) {
        this.resultCode = resultCode;
    }

    public SyncStatus(int resultCode, Exception exception) {
        this.resultCode = resultCode;
        this.exception = exception;
    }

    public int getResultCode() {
        return resultCode;
    }

    public Exception getException() {
        return exception;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SyncStatus that = (SyncStatus) o;
        return getResultCode() == that.getResultCode() &&
                Objects.equals(getException(), that.getException());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getResultCode(), getException());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SyncStatus{");
        sb.append("resultCode=").append(resultCode);
        sb.append(", exception=").append(exception);
        sb.append('}');
        return sb.toString();
    }
}
