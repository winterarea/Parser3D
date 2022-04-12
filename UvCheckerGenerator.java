package com.nmaid.asset.procedural;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import sun.org.mozilla.javascript.internal.NativeArray;

public class UvCheckerGenerator {
	public static void main(String[] args) throws FileNotFoundException, IOException, NoSuchMethodException {
		//colorFromExppression(1024,8,1,1,1,1);
		ColorfulUvMap();
	}
	public static float[] colorFromExppression(int resolution,int gridSize,int x,int y,int offsetX,int offsetY) throws NoSuchMethodException{
		sun.org.mozilla.javascript.internal.NativeArray result=null;
		//float angle=(float) ((Math.atan2(gridWidth*i+cordOffset-resolution/2, -(gridWidth*j+cordOffset-resolution/2))+Math.PI)/2/Math.PI);
		//float distance=(float) ((1.0f*Math.abs(grid/2-i)/(grid/2)+1.0*Math.abs(grid/2-j)/(grid/2))/2)/4+0.75f;
		String regular="function hueCircle(resolution,gridX,x,y,offsetX,offsetY){ " +
				"var gridWidth=resolution/gridX;" +
				"var hue=(Math.atan2(gridWidth*x+offsetX-resolution/2,-(gridWidth*y+offsetX-resolution/2))+Math.PI)/2/Math.PI;" +
				"var saturation=1;" +
				"var brightness=((Math.abs(gridX/2-x)/(gridX/2)+Math.abs(gridX/2-y)/(gridX/2))/2)/4+0.75;" +
				"return [hue,saturation,brightness*0.8];" +
				"}";
		//安全隐患：https://blog.csdn.net/xiao_jun_0820/article/details/76498268. 
		//https://www.thinbug.com/q/20793089
		//NashornSandbox 代替方案
		String dangerCode="java.lang.System.out.println(\"Hello World\");";
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
		try {
			engine.eval(regular);
			if (engine instanceof Invocable) {
				Invocable invoke = (Invocable) engine;
				result = (NativeArray) invoke.invokeFunction("hueCircle",resolution,gridSize,x,y,offsetX,offsetY);
				for(int i=0;i<result.getLength();i++){
					System.out.print(result.get(i, null)+",");
				}
				System.out.println();
			} else {
				System.out.println("error");
			}
		}catch (ScriptException e) {
			System.out.println("表达式runtime错误:" + e.getMessage());
		}
		if(result!=null)
			return new float[]{Float.parseFloat(result.get(0,null).toString()),Float.parseFloat(result.get(1,null).toString()),Float.parseFloat(result.get(2,null).toString())};
		else
			return new float[]{0,0,1};
	}
	public static void ColorfulUvMap() throws FileNotFoundException, IOException, NoSuchMethodException{
		int resolution=512;
		int grid = 16;
		int subGrid=8;
		int cordOffset=2;//left top corner offset
		int cornerScale=4;//round corner radius
		int gridWidth=resolution/grid;
		float hue = 0,saturation = 1,brightness = 1;
		BufferedImage bi=new BufferedImage(resolution,resolution,BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2=(Graphics2D) bi.getGraphics();
		//https://bbs.csdn.net/topics/396340978
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Color color=Color.getHSBColor(0, 0, 1);//0.125
		g2.setColor(color);
		g2.fillRect(0, 0, resolution, resolution);
		//g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
		/*GraphicsEnvironment fontEnv=GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames=fontEnv.getAvailableFontFamilyNames();
		for(int i=0;i<fontNames.length;i++){
			System.out.println(fontNames[i]);
		}*/
		for(int i=0;i<grid;i++){
			for(int j=0;j<grid;j++){
				hue=(8*i+j)*1.0f/64;
				float angle=(float) ((Math.atan2(gridWidth*i+cordOffset-resolution/2, -(gridWidth*j+cordOffset-resolution/2))+Math.PI)/2/Math.PI);
				float distance=(float) ((1.0f*Math.abs(grid/2-i)/(grid/2)+1.0*Math.abs(grid/2-j)/(grid/2))/2)/4+0.75f;
				float[] colorBytes=colorFromExppression(resolution,grid,i,j,cordOffset,cordOffset);
				color=Color.getHSBColor(angle, saturation, distance/2);
				color=Color.getHSBColor(colorBytes[0], colorBytes[1], colorBytes[2]);
				g2.setColor(color);
				g2.fillRoundRect(gridWidth*i+cordOffset, gridWidth*j+cordOffset, gridWidth-cordOffset*2, gridWidth-cordOffset*2,cordOffset*cornerScale,cordOffset*cornerScale);
				//g2.fillArc(gridWidth*i, gridWidth*j, gridWidth, gridWidth, 0, 360);
				
				color=Color.getHSBColor(0, 0, 1);
				g2.setColor(color);
				Font logoFont = new Font("Arial", Font.PLAIN, 12);
				FontMetrics metrics = g2.getFontMetrics(logoFont);
				String text=String.valueOf((char)('A'+grid-1-j))+Integer.toHexString(i).toUpperCase();
				int width=metrics.stringWidth(text);
				g2.drawString(text, gridWidth*i+gridWidth/2-width/2, gridWidth*j+gridWidth/2+(metrics.getHeight()/ 2 - metrics.getDescent()) );
			}
		}
		g2.dispose();

		ImageIO.write(bi, "PNG", new FileOutputStream("G:/NMaidData/UvChecker.png"));
	}
}
