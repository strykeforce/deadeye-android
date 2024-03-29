package org.team2767.deadeye.di;

import android.content.Context;
import dagger.Component;
import javax.inject.Singleton;
import org.strykeforce.thirdcoast.deadeye.rx.RxBus;
import org.team2767.deadeye.DeadeyeRendererFactory;
import org.team2767.deadeye.Network;
import org.team2767.deadeye.Settings;
import org.team2767.deadeye.opengl.CameraShaderProgram;
import org.team2767.deadeye.opengl.TextureShaderProgram;

/** Dagger dependency-injection component. */
@Component(modules = {ContextModule.class})
@Singleton
public interface SingletonComponent {

  Context appContext();

  RxBus bus();

  Network network();

  TextureShaderProgram textureShaderProgram();

  CameraShaderProgram cameraShaderProgram();

  DeadeyeRendererFactory deadeyeRendererFactory();

  Settings settings();
}
