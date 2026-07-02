package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Reflective client bridge for mapping and API drift across supported MC versions.
 */
public final class ClientRuntimeCompat {
  private ClientRuntimeCompat() {
  }

  public static void showOverlayMessage(Minecraft minecraft, Component message) {
    if (minecraft == null || minecraft.gui == null || message == null) {
      return;
    }

    Object gui = minecraft.gui;
    Method oneArg = null;
    Method twoArg = null;

    for (Method method : gui.getClass().getMethods()) {
      if (Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      Class<?>[] params = method.getParameterTypes();
      if (params.length == 1 && Component.class.isAssignableFrom(params[0])) {
        oneArg = method;
      } else if (params.length == 2
          && Component.class.isAssignableFrom(params[0])
          && (params[1] == boolean.class || params[1] == Boolean.class)) {
        twoArg = method;
      }
    }

    try {
      if (twoArg != null) {
        twoArg.invoke(gui, message, false);
        return;
      }
      if (oneArg != null) {
        oneArg.invoke(gui, message);
      }
    } catch (ReflectiveOperationException ignored) {
    }
  }

  public static void setScreen(Minecraft minecraft, Screen screen) {
    if (minecraft == null) {
      return;
    }

    Method preferred = null;
    Method fallback = null;
    for (Method method : minecraft.getClass().getMethods()) {
      if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 1) {
        continue;
      }
      if (!Screen.class.isAssignableFrom(method.getParameterTypes()[0])) {
        continue;
      }

      if ("setScreen".equals(method.getName())) {
        preferred = method;
        break;
      }
      if ("setScreenAndShow".equals(method.getName())) {
        preferred = method;
        continue;
      }
      if (preferred == null && method.getName().toLowerCase(java.util.Locale.ROOT).contains("screen")) {
        preferred = method;
      }
      if (fallback == null) {
        fallback = method;
      }
    }

    try {
      if (preferred != null) {
        preferred.invoke(minecraft, screen);
        return;
      }
      if (fallback != null) {
        fallback.invoke(minecraft, screen);
      }
    } catch (ReflectiveOperationException ignored) {
    }
  }

  public static Vec3 getCameraPosition(Minecraft minecraft) {
    if (minecraft == null || minecraft.gameRenderer == null) {
      return Vec3.ZERO;
    }

    Object camera = invokeNoArg(minecraft.gameRenderer, "getMainCamera");
    if (camera == null) {
      camera = invokeNoArg(minecraft.gameRenderer, "getCamera");
    }

    if (camera != null) {
      Object position = invokeNoArg(camera, "position");
      if (position == null) {
        position = invokeNoArg(camera, "getPosition");
      }
      if (position instanceof Vec3 vec3) {
        return vec3;
      }
    }

    return Vec3.ZERO;
  }

  public static Screen getCurrentScreen(Minecraft minecraft) {
    if (minecraft == null) {
      return null;
    }

    Screen direct = getFieldValueByNameAndType(minecraft, "screen", Screen.class);
    if (direct != null) {
      return direct;
    }

    for (Method method : minecraft.getClass().getMethods()) {
      if (method.getParameterCount() == 0
          && Screen.class.isAssignableFrom(method.getReturnType())
          && method.getName().toLowerCase(java.util.Locale.ROOT).contains("screen")) {
        try {
          Object value = method.invoke(minecraft);
          if (value instanceof Screen typed) {
            return typed;
          }
        } catch (ReflectiveOperationException ignored) {
        }
      }
    }

    return null;
  }

  public static Object getBufferSource(Minecraft minecraft) {
    if (minecraft == null) {
      return null;
    }

    Object renderBuffers = invokeNoArg(minecraft, "renderBuffers");
    if (renderBuffers == null) {
      renderBuffers = invokeNoArg(minecraft, "getRenderBuffers");
    }
    if (renderBuffers == null) {
      renderBuffers = findFieldValueByTypeName(minecraft, "RenderBuffers");
    }

    if (renderBuffers == null) {
      return null;
    }

    Object bufferSource = invokeNoArg(renderBuffers, "bufferSource");
    if (bufferSource == null) {
      bufferSource = invokeNoArg(renderBuffers, "getBufferSource");
    }
    return bufferSource;
  }

  public static Object getBuffer(Object bufferSource, Object renderType) {
    if (bufferSource == null || renderType == null) {
      return null;
    }

    try {
      for (Method method : bufferSource.getClass().getMethods()) {
        if (!"getBuffer".equals(method.getName()) || method.getParameterCount() != 1) {
          continue;
        }
        return method.invoke(bufferSource, renderType);
      }
    } catch (ReflectiveOperationException ignored) {
    }
    return null;
  }

  public static void endBatch(Object bufferSource, Object renderType) {
    if (bufferSource == null || renderType == null) {
      return;
    }

    try {
      for (Method method : bufferSource.getClass().getMethods()) {
        if (!"endBatch".equals(method.getName()) || method.getParameterCount() != 1) {
          continue;
        }
        method.invoke(bufferSource, renderType);
        return;
      }
    } catch (ReflectiveOperationException ignored) {
    }
  }

  public static void renderShape(PoseStack poseStack, Object vertexConsumer, VoxelShape shape, int color,
      float lineWidth) {
    if (poseStack == null || vertexConsumer == null || shape == null) {
      return;
    }

    try {
      Class<?> shapeRenderer = Class.forName("net.minecraft.client.renderer.ShapeRenderer");
      for (Method method : shapeRenderer.getMethods()) {
        if (!"renderShape".equals(method.getName()) || method.getParameterCount() != 8) {
          continue;
        }
        method.invoke(null, poseStack, vertexConsumer, shape, 0.0d, 0.0d, 0.0d, color, lineWidth);
        return;
      }
    } catch (ReflectiveOperationException | LinkageError ignored) {
    }
  }

  private static Object invokeNoArg(Object instance, String methodName) {
    try {
      Method method = instance.getClass().getMethod(methodName);
      return method.invoke(instance);
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }

  private static Object findFieldValueByTypeName(Object instance, String typeSuffix) {
    Class<?> current = instance.getClass();
    while (current != null) {
      for (Field field : current.getDeclaredFields()) {
        if (!field.getType().getSimpleName().endsWith(typeSuffix)) {
          continue;
        }
        try {
          field.setAccessible(true);
          Object value = field.get(instance);
          if (value != null) {
            return value;
          }
        } catch (ReflectiveOperationException ignored) {
        }
      }
      current = current.getSuperclass();
    }
    return null;
  }

  private static <T> T getFieldValueByNameAndType(Object instance, String fieldName, Class<T> type) {
    Class<?> current = instance.getClass();
    while (current != null) {
      try {
        Field field = current.getDeclaredField(fieldName);
        if (!type.isAssignableFrom(field.getType())) {
          return null;
        }
        field.setAccessible(true);
        Object value = field.get(instance);
        if (type.isInstance(value)) {
          return type.cast(value);
        }
        return null;
      } catch (NoSuchFieldException ignored) {
      } catch (ReflectiveOperationException ignored) {
        return null;
      }
      current = current.getSuperclass();
    }
    return null;
  }

}