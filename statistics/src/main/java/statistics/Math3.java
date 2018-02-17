package statistics;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.math.plot.Plot2DPanel;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.github.sh0nk.matplotlib4j.Plot;
import com.github.sh0nk.matplotlib4j.PythonExecutionException;

public class Math3 {

	public static void main(String[] args) throws Exception {
		Path path = Paths.get(Thread.currentThread().getContextClassLoader().getResource("hm51106year.csv").toURI());
		Path path1 = Paths.get(Thread.currentThread().getContextClassLoader().getResource("generation.csv").toURI());

		CsvMapper mapper = new CsvMapper();
		// important: we need "array wrapping" (see next section) here:
		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
		mapper.enable(CsvParser.Feature.IGNORE_TRAILING_UNMAPPABLE);

		ArrayList<Double> solarRadiation = new ArrayList<>();

		MappingIterator<String[]> it = mapper.readerFor(String[].class).readValues(path.toFile());
		while (it.hasNext()) {
			String[] row = it.next();
			if (false == StringUtils.equals(row[0], "1")) {
				continue;
			}
			// System.out.println(StringUtils.join(row, ","));
			for (int i = 4; i < 4 + 24; i++) {
				solarRadiation.add(Double.valueOf(row[i]));
			}
		}
		System.out.println(solarRadiation.size());

		ArrayList<Double> generatingCapacity = new ArrayList<>();
		it = mapper.readerFor(String[].class).readValues(path1.toFile());
		while (it.hasNext()) {
			String[] row = it.next();
			// System.out.println(StringUtils.join(row, ","));
			for (int i = 1; i < 1 + 24; i++) {
				generatingCapacity.add(Double.valueOf(row[i]) * 4.2d * 1000);
			}
		}
		System.out.println(generatingCapacity.size());

		SimpleRegression regression = new SimpleRegression();
		// double[][] data = { { 1, 5.1 }, { 2, 7.9 }, { 3, 10.5 }, { 4, 14.2 }, { 0, 0
		// }, { 0, 0 }, { 0, 0 } };
		System.out.println("日射量=0を除かない");
		for (int i = 0; i < 24 * 365; i++) {
			regression.addData(solarRadiation.get(i).doubleValue(), generatingCapacity.get(i).doubleValue());
		}
		// 回帰直線の切片
		System.out.println("回帰直線の切片 = " + regression.getIntercept());
		// 回帰直線の傾き
		System.out.println("回帰直線の傾き = " + regression.getSlope());
		// 標準誤差
		System.out.println("標準誤差 = " + regression.getSlopeStdErr());
		// 寄与率
		System.out.println("寄与率 = " + regression.getRSquare());
		System.out.println("");

		regression.clear();
		System.out.println("日射量=0を除く");
		for (int i = 0; i < solarRadiation.size(); i++) {
			if (solarRadiation.get(i).doubleValue() == 0d) {
				continue;
			}
			regression.addData(solarRadiation.get(i).doubleValue(), generatingCapacity.get(i).doubleValue());
		}
		// 回帰直線の切片
		System.out.println("回帰直線の切片 = " + regression.getIntercept());
		// 回帰直線の傾き
		System.out.println("回帰直線の傾き = " + regression.getSlope());
		// 標準誤差
		System.out.println("標準誤差 = " + regression.getSlopeStdErr());
		// 寄与率
		System.out.println("寄与率 = " + regression.getRSquare());
		System.out.println("");

		// drawChartByJmathplot(solarRadiation, generatingCapacity);

		// drawChartByJFrame(solarRadiation, generatingCapacity,
		// regression.getIntercept(), regression.getSlope());

		// drawChartByMatplotlib4j(solarRadiation, generatingCapacity);
	}

	private static void drawChartByMatplotlib4j(ArrayList<Double> solarRadiation, ArrayList<Double> generatingCapacity)
			throws IOException, PythonExecutionException {
		Plot plt = Plot.create();
		plt.plot().add(solarRadiation, generatingCapacity, "o").label("solarRadiation - generatingCapacity");
		plt.legend().loc("upper right");
		plt.title("scatter");
		plt.show();
	}

	private static void drawChartByJFrame(ArrayList<Double> solarRadiation, ArrayList<Double> generatingCapacity,
			double intercepter, double slope) {
		// データ作成
		List<Point2D.Double> points = new ArrayList<Point2D.Double>();
		int n = solarRadiation.size();
		for (int i = 0; i < n; i++) {
			points.add(
					new Point2D.Double(solarRadiation.get(i).doubleValue(), generatingCapacity.get(i).doubleValue()));
		}
		// グラフ作成
		BufferedImage img = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, 400, 300);
		g.setColor(Color.RED);
		for (Point2D p : points) {
			System.out.println(ToStringBuilder.reflectionToString(p));
			double x = p.getX();
			double y = p.getY();
			g.fillOval((int) y + 20, 280 - (int) x, 5, 5);
		}

		g.setColor(Color.BLUE);
		g.drawLine(20, 280 - (int) intercepter, 20 + 300, 280 - (int) (intercepter + slope * 300));

		// 表示
		JFrame f = new JFrame("線形回帰分析");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.add(new JLabel(new ImageIcon(img)));
		f.setSize(400, 300);
		f.setVisible(true);
	}

	private static void drawChartByJmathplot(ArrayList<Double> solarRadiation, ArrayList<Double> generatingCapacity) {
		// データ作成
		// create your PlotPanel (you can use it as a JPanel)
		Plot2DPanel plot = new Plot2DPanel();

		// add a line plot to the PlotPanel
		plot.addScatterPlot("my plot", solarRadiation.stream().mapToDouble(d -> d).toArray(),
				generatingCapacity.stream().mapToDouble(d -> d).toArray());

		// put the PlotPanel in a JFrame, as a JPanel
		JFrame frame = new JFrame("a plot panel");
		frame.setContentPane(plot);
		frame.setVisible(true);
	}

}
