package dev.heliosclient.managers;

import dev.heliosclient.HeliosClient;
import dev.heliosclient.event.Event;
import dev.heliosclient.event.SubscribeEvent;
import dev.heliosclient.event.events.input.MouseClickEvent;
import dev.heliosclient.event.listener.Listener;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import java.lang.invoke.*;

public class EventManager {
    private static final Map<Listener, Map<Class<?>, List<MethodHandle>>> INSTANCE = new ConcurrentHashMap<>();
    private static final Comparator<MethodHandle> METHOD_COMPARATOR = Comparator.comparingInt(mh -> {
        SubscribeEvent annotation = mh.type().parameterType(0).getAnnotation(SubscribeEvent.class);
        return (annotation != null) ? annotation.priority().ordinal() : Integer.MAX_VALUE;
    });
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static void register(Listener listener) {
        Map<Class<?>, List<MethodHandle>> listenerMethods = new HashMap<>();
        for (Method method : listener.getClass().getMethods()) {
            if (method.isAnnotationPresent(SubscribeEvent.class) && method.getParameterCount() == 1) {
                Class<?> eventType = method.getParameterTypes()[0];
                MethodHandle methodHandle = getMethodHandle(method);
                List<MethodHandle> methodHandles = listenerMethods.computeIfAbsent(eventType, k -> new ArrayList<>());
                methodHandles.add(methodHandle);
                methodHandles.sort(METHOD_COMPARATOR);
            }
        }
        INSTANCE.put(listener, listenerMethods);
    }


    private static MethodHandle getMethodHandle(Method method) {
        try {
            return LOOKUP.unreflect(method);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void unregister(Listener listener) {
        INSTANCE.remove(listener);
    }

    public static void postEvent(Event event) {
        Class<?> eventType = event.getClass();
        for (Map.Entry<Listener, Map<Class<?>, List<MethodHandle>>> entry : INSTANCE.entrySet()) {
            List<MethodHandle> methodHandles = entry.getValue().get(eventType);
            if (methodHandles != null) {
                for (MethodHandle methodHandle : methodHandles) {
                    try {
                        methodHandle.invoke(entry.getKey(), event);
                    } catch (Throwable e) {
                        handleException(e, entry.getKey(), event);
                    }
                }
            }
        }
    }

    private static void handleException(Throwable e, Listener listener, Event event) {
        HeliosClient.LOGGER.info("Exception occurred while processing event: " + event.getClass().getName() + " \n Following was the listener: " + listener, e);
        HeliosClient.LOGGER.warn("An error occurred while processing an event. Please check the log file for details.");
    }

}

