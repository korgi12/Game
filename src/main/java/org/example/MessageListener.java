package org.example;

@FunctionalInterface
interface MessageListener {
    void onMessage(Response message);
}
