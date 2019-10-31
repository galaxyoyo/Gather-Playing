module GatherPlaying {
	requires javafx.base;
	requires javafx.graphics;
	requires javafx.swing;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.web;
	requires lombok;
	requires com.google.common;
	requires org.apache.commons.io;
	requires org.apache.commons.lang3;
	requires io.netty.all;
	requires com.google.gson;
	requires annotations;
	//requires com.gluonhq.glisten.afterburner;
	//requires com.gluonhq.charm.glisten;

	exports fr.galaxyoyo.gatherplaying;

	opens fr.galaxyoyo.gatherplaying to com.google.gson;
	opens fr.galaxyoyo.gatherplaying.client to javafx.graphics;
	opens fr.galaxyoyo.gatherplaying.client.gui to javafx.fxml;
}