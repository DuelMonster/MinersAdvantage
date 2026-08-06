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
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * Reflective client bridge for mapping and API drift across supported MC versions.
 */
public final class ClientRuntimeCompat {
  private static volatile boolean loggedHudOverlayFallback;
  private static volatile boolean loggedShapeRenderFallback;
  private static volatile Method cachedCurrentScreenAccessor;
  private static volatile boolean currentScreenAccessorResolved;

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
        return;
      }
    } catch (ReflectiveOperationException ignored) {
    }

    Object hud = findHud(gui);
    if (hud != null) {
      Method hudTwoArg = findMethodByComponentSignature(hud, true);
      Method hudOneArg = findMethodByComponentSignature(hud, false);
      try {
        if (hudTwoArg != null) {
          hudTwoArg.invoke(hud, message, false);
          logHudFallbackOnce(hudTwoArg.getName());
          return;
        }
        if (hudOneArg != null) {
          hudOneArg.invoke(hud, message);
          logHudFallbackOnce(hudOneArg.getName());
          return;
        }
      } catch (ReflectiveOperationException ignored) {
      }
    }

    if (minecraft.player != null && showPlayerOverlayMessage(minecraft.player, message)) {
      logHudFallbackOnce("player-message-reflection");
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
    if (minecraft == null) {
      return Vec3.ZERO;
    }

    if (minecraft.gameRenderer == null) {
      return minecraft.player != null ? minecraft.player.getEyePosition() : Vec3.ZERO;
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
      if (position == null) {
        position = invokeFirstNoArgByReturnType(camera, Vec3.class);
      }
      if (position == null) {
        position = getFieldValueByNameAndType(camera, "position", Vec3.class);
      }
      if (position == null) {
        position = findFieldValueByType(camera, Vec3.class);
      }
      if (position instanceof Vec3 vec3) {
        return vec3;
      }
    }

    return minecraft.player != null ? minecraft.player.getEyePosition() : Vec3.ZERO;
  }

  public static Screen getCurrentScreen(Minecraft minecraft) {
    if (minecraft == null) {
      return null;
    }

    Screen direct = getFieldValueByNameAndType(minecraft, "screen", Screen.class);
    if (direct != null) {
      return direct;
    }

    Method accessor = resolveCurrentScreenAccessor(minecraft);
    if (accessor == null) {
      return null;
    }

    try {
      Object value = accessor.invoke(minecraft);
      if (value instanceof Screen typed) {
        return typed;
      }
    } catch (ReflectiveOperationException ignored) {
    }

    return null;
  }

  private static Method resolveCurrentScreenAccessor(Minecraft minecraft) {
    if (currentScreenAccessorResolved) {
      return cachedCurrentScreenAccessor;
    }

    if (minecraft == null) {
      currentScreenAccessorResolved = true;
      return null;
    }

    for (Method method : minecraft.getClass().getMethods()) {
      if (method.getParameterCount() == 0
          && Screen.class.isAssignableFrom(method.getReturnType())
          && method.getName().toLowerCase(java.util.Locale.ROOT).contains("screen")) {
        cachedCurrentScreenAccessor = method;
        currentScreenAccessorResolved = true;
        return cachedCurrentScreenAccessor;
      }
    }

    currentScreenAccessorResolved = true;
    return null;
  }

  public static Object getBufferSource(Minecraft minecraft) {
    return getBufferSource(null, minecraft);
  }

  public static Object getBufferSource(Object renderContext, Minecraft minecraft) {
    Object contextBufferSource = getBufferSourceFromRenderContext(renderContext);
    if (contextBufferSource != null) {
      return contextBufferSource;
    }

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

  public static Object getSubmitNodeCollector(Object renderContext) {
    if (renderContext == null) {
      return null;
    }

    Object collector = invokeNoArg(renderContext, "submitNodeCollector");
    if (collector != null) {
      return collector;
    }

    return findFieldValueByTypeName(renderContext, "SubmitNodeCollector");
  }

  public static boolean submitShapeOutline(Object submitNodeCollector, PoseStack poseStack, VoxelShape shape,
      Object renderType, int color, float lineWidth, boolean highContrast) {
    if (submitNodeCollector == null || poseStack == null || shape == null || renderType == null) {
      return false;
    }

    try {
      for (Method method : submitNodeCollector.getClass().getMethods()) {
        if (!"submitShapeOutline".equals(method.getName()) || method.getParameterCount() != 6) {
          continue;
        }
        method.invoke(submitNodeCollector, poseStack, shape, renderType, color, lineWidth, highContrast);
        return true;
      }
    } catch (ReflectiveOperationException ignored) {
    }

    return false;
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

    // 26.2 removed ShapeRenderer; render lines directly from voxel edges.
    renderShapeFallbackByEdges(poseStack, vertexConsumer, shape, color, lineWidth);
  }

  private static Object findHud(Object gui) {
    Object direct = getFieldValueByName(gui, "hud");
    if (direct != null) {
      return direct;
    }
    Object byMethod = invokeNoArg(gui, "hud");
    if (byMethod != null) {
      return byMethod;
    }
    return findFieldValueByTypeName(gui, "Hud");
  }

  private static Method findMethodByComponentSignature(Object instance, boolean twoArg) {
    for (Method method : instance.getClass().getMethods()) {
      if (Modifier.isStatic(method.getModifiers())) {
        continue;
      }
      Class<?>[] params = method.getParameterTypes();
      if (twoArg) {
        if (params.length == 2
            && Component.class.isAssignableFrom(params[0])
            && (params[1] == boolean.class || params[1] == Boolean.class)) {
          return method;
        }
      } else if (params.length == 1 && Component.class.isAssignableFrom(params[0])) {
        return method;
      }
    }
    return null;
  }

  private static boolean showPlayerOverlayMessage(Object player, Component message) {
    if (player == null || message == null) {
      return false;
    }

    // Newer/older mappings differ on method names, so resolve by signature.
    for (Method method : player.getClass().getMethods()) {
      if (Modifier.isStatic(method.getModifiers())) {
        continue;
      }

      Class<?>[] params = method.getParameterTypes();
      try {
        if (params.length == 2
            && Component.class.isAssignableFrom(params[0])
            && (params[1] == boolean.class || params[1] == Boolean.class)) {
          method.invoke(player, message, true);
          return true;
        }
        if (params.length == 1 && Component.class.isAssignableFrom(params[0])) {
          method.invoke(player, message);
          return true;
        }
      } catch (ReflectiveOperationException ignored) {
      }
    }
    return false;
  }

  private static void logHudFallbackOnce(String source) {
    if (loggedHudOverlayFallback) {
      return;
    }
    loggedHudOverlayFallback = true;
    LogUtils.logDebug("Overlay message routed through fallback source={}", source);
  }

  private static void renderShapeFallbackByEdges(PoseStack poseStack, Object vertexConsumer, VoxelShape shape,
      int color, float lineWidth) {
    Method addVertexRaw = findMethod(vertexConsumer.getClass(), "addVertex", float.class, float.class, float.class);
    Method addVertexWithPose = findAddVertexWithPose(vertexConsumer.getClass());
    Method setColorRgba = findMethod(vertexConsumer.getClass(), "setColor", int.class, int.class, int.class,
        int.class);
    Method setNormal = findMethod(vertexConsumer.getClass(), "setNormal", float.class, float.class, float.class);
    Method setLineWidth = findMethod(vertexConsumer.getClass(), "setLineWidth", float.class);
    Object pose = resolveCurrentPose(poseStack);
    if ((addVertexRaw == null && addVertexWithPose == null) || setColorRgba == null || setNormal == null) {
      return;
    }

    int alpha = (color >>> 24) & 0xFF;
    int red = (color >>> 16) & 0xFF;
    int green = (color >>> 8) & 0xFF;
    int blue = color & 0xFF;
    if (alpha == 0) {
      alpha = 255;
    }

    if (!loggedShapeRenderFallback) {
      loggedShapeRenderFallback = true;
      LogUtils.logDebug("Shape rendering using edge fallback path.");
    }

    Method addVertexRawMethod = addVertexRaw;
    Method addVertexWithPoseMethod = addVertexWithPose;
    Method setColorMethod = setColorRgba;
    Method setNormalMethod = setNormal;
    Method setLineWidthMethod = setLineWidth;
    Object poseSnapshot = pose;
    final int finalAlpha = alpha;

    shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
      float nx = (float) (x2 - x1);
      float ny = (float) (y2 - y1);
      float nz = (float) (z2 - z1);
      float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
      if (length > 0.0f) {
        nx /= length;
        ny /= length;
        nz /= length;
      }

      emitEdgeVertex(vertexConsumer, poseSnapshot, addVertexWithPoseMethod, addVertexRawMethod, setColorMethod,
          setNormalMethod, setLineWidthMethod,
          (float) x1, (float) y1, (float) z1, red, green, blue, finalAlpha, nx, ny, nz, lineWidth);
      emitEdgeVertex(vertexConsumer, poseSnapshot, addVertexWithPoseMethod, addVertexRawMethod, setColorMethod,
          setNormalMethod, setLineWidthMethod,
          (float) x2, (float) y2, (float) z2, red, green, blue, finalAlpha, nx, ny, nz, lineWidth);
    });
  }

  private static void emitEdgeVertex(
      Object consumer,
      Object pose,
      Method addVertexWithPose,
      Method addVertexRaw,
      Method setColor,
      Method setNormal,
      Method setLineWidth,
      float x,
      float y,
      float z,
      int red,
      int green,
      int blue,
      int alpha,
      float nx,
      float ny,
      float nz,
      float lineWidth) {
    try {
      Object chain;
      if (addVertexWithPose != null && pose != null) {
        chain = addVertexWithPose.invoke(consumer, pose, x, y, z);
      } else if (addVertexRaw != null) {
        chain = addVertexRaw.invoke(consumer, x, y, z);
      } else {
        return;
      }
      if (chain == null) {
        chain = consumer;
      }
      setColor.invoke(chain, red, green, blue, alpha);
      setNormal.invoke(chain, nx, ny, nz);
      if (setLineWidth != null) {
        try {
          setLineWidth.invoke(chain, lineWidth);
        } catch (ReflectiveOperationException ignored) {
        }
      }
    } catch (ReflectiveOperationException ignored) {
    }
  }

  private static Method findAddVertexWithPose(Class<?> consumerType) {
    for (Method method : consumerType.getMethods()) {
      if (!"addVertex".equals(method.getName()) || method.getParameterCount() != 4) {
        continue;
      }
      Class<?>[] params = method.getParameterTypes();
      if (params[0].getName().endsWith("PoseStack$Pose")
          && params[1] == float.class
          && params[2] == float.class
          && params[3] == float.class) {
        return method;
      }
    }
    return null;
  }

  private static Object resolveCurrentPose(PoseStack poseStack) {
    if (poseStack == null) {
      return null;
    }

    Object pose = invokeNoArg(poseStack, "last");
    if (pose != null) {
      return pose;
    }
    return invokeNoArg(poseStack, "lastPose");
  }

  private static Object getBufferSourceFromRenderContext(Object renderContext) {
    if (renderContext == null) {
      return null;
    }

    Object direct = invokeNoArg(renderContext, "consumers");
    if (hasGetBufferMethod(direct)) {
      return direct;
    }

    direct = invokeNoArg(renderContext, "bufferSource");
    if (hasGetBufferMethod(direct)) {
      return direct;
    }

    direct = invokeNoArg(renderContext, "vertexConsumers");
    if (hasGetBufferMethod(direct)) {
      return direct;
    }

    for (Method method : renderContext.getClass().getMethods()) {
      if (method.getParameterCount() != 0) {
        continue;
      }
      Object candidate;
      try {
        candidate = method.invoke(renderContext);
      } catch (ReflectiveOperationException ignored) {
        continue;
      }
      if (hasGetBufferMethod(candidate)) {
        return candidate;
      }
    }

    return null;
  }

  private static boolean hasGetBufferMethod(Object instance) {
    if (instance == null) {
      return false;
    }

    for (Method method : instance.getClass().getMethods()) {
      if ("getBuffer".equals(method.getName()) && method.getParameterCount() == 1) {
        return true;
      }
    }

    return false;
  }

  private static Object invokeNoArg(Object instance, String methodName) {
    try {
      Method method = instance.getClass().getMethod(methodName);
      return method.invoke(instance);
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }

  private static Method findMethod(Class<?> type, String methodName, Class<?>... parameterTypes) {
    try {
      return type.getMethod(methodName, parameterTypes);
    } catch (ReflectiveOperationException ignored) {
      return null;
    }
  }

  private static Object invokeFirstNoArgByReturnType(Object instance, Class<?> returnType) {
    if (instance == null || returnType == null) {
      return null;
    }

    for (Method method : instance.getClass().getMethods()) {
      if (method.getParameterCount() != 0 || !returnType.isAssignableFrom(method.getReturnType())) {
        continue;
      }
      try {
        return method.invoke(instance);
      } catch (ReflectiveOperationException ignored) {
      }
    }
    return null;
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

  private static Object findFieldValueByType(Object instance, Class<?> fieldType) {
    if (instance == null || fieldType == null) {
      return null;
    }

    Class<?> current = instance.getClass();
    while (current != null) {
      for (Field field : current.getDeclaredFields()) {
        if (!fieldType.isAssignableFrom(field.getType())) {
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

  private static Object getFieldValueByName(Object instance, String fieldName) {
    Class<?> current = instance.getClass();
    while (current != null) {
      try {
        Field field = current.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(instance);
      } catch (NoSuchFieldException ignored) {
      } catch (ReflectiveOperationException ignored) {
        return null;
      }
      current = current.getSuperclass();
    }
    return null;
  }

}