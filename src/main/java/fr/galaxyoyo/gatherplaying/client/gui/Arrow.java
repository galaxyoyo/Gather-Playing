package fr.galaxyoyo.gatherplaying.client.gui;

import fr.galaxyoyo.gatherplaying.client.Client;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.QuadCurve;

public class Arrow extends AnchorPane
{
	private final DoubleProperty x1 = new SimpleDoubleProperty();
	private final DoubleProperty x2 = new SimpleDoubleProperty();
	private final DoubleProperty y1 = new SimpleDoubleProperty();
	private final DoubleProperty y2 = new SimpleDoubleProperty();
	private final QuadCurve line = new QuadCurve();
	private final ImageView head = new ImageView(getClass().getResource("/icons/arrowhead.png").toString());

	public Arrow()
	{
		super();
		line.setStroke(Color.RED);
		line.setFill(null);
		line.startXProperty().bind(x1);
		line.startYProperty().bind(y1);
		line.endXProperty().bind(x2);
		line.endYProperty().bind(y2);
		line.controlXProperty().bind(x1Property().add(x2Property()).divide(Bindings.when(x1.greaterThanOrEqualTo(x2)).then(1.5).otherwise(3)));
		line.controlYProperty().bind(y1Property().add(y2Property()).divide(2));
		head.translateXProperty().bind(line.endXProperty().subtract(8));
		head.translateYProperty().bind(line.endYProperty().subtract(8));
		head.rotateProperty().bind(Bindings.when(x1.greaterThanOrEqualTo(x2)).then(Bindings.createDoubleBinding(() -> Math
						.toDegrees(Math.acos((getY2() - line.getControlY()) / new Point2D(line.getControlX(), line.getControlY()).distance(new Point2D(getX2(), getY2()))) - Math
								.PI),
				line.controlXProperty(), line.controlYProperty(), x2Property(), y2Property())).otherwise(Bindings.createDoubleBinding(
				() -> new Point2D(line.getControlX(), line.getControlY()).angle(new Point2D(line.getControlX(), line.getControlY() - 1), new Point2D(getX2(), getY2())),
				line.controlXProperty(), line.controlYProperty(), x2Property(), y2Property())));
		getChildren().add(line);
		getChildren().add(head);
		DropShadow ds = new DropShadow();
		ds.setOffsetY(2.0D);
		ds.setColor(Color.RED);
		ds.setBlurType(BlurType.GAUSSIAN);
		//	setEffect(ds);
		setPickOnBounds(false);
		Client.getStackPane().getChildren().add(this);
	}

	private DoubleProperty x1Property()
	{
		return x1;
	}

	private DoubleProperty x2Property()
	{
		return x2;
	}

	private DoubleProperty y1Property()
	{
		return y1;
	}

	private DoubleProperty y2Property()
	{
		return y2;
	}

	private double getY2()
	{
		return y2.get();
	}

	private double getX2()
	{
		return x2.get();
	}

	public void setX2(double x2)
	{
		this.x2.set(x2);
	}

	public void setY2(double y2)
	{
		this.y2.set(y2);
	}

	public double getX1()
	{
		return x1.get();
	}

	public void setX1(double x1)
	{
		this.x1.set(x1);
	}

	public double getY1()
	{
		return y1.get();
	}

	public void setY1(double y1)
	{
		this.y1.set(y1);
	}
}
