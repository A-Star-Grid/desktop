package org.example.server.models;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class VirtualMachine {
    private String ip;
    private String name;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void setIp(String newIp) {
        lock.writeLock().lock();
        try {
            this.ip = newIp;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getIp() {
        lock.readLock().lock();
        try {
            return ip;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setName(String newName) {
        lock.writeLock().lock();
        try {
            this.name = newName;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getName() {
        lock.readLock().lock();
        try {
            return name;
        } finally {
            lock.readLock().unlock();
        }
    }
}
