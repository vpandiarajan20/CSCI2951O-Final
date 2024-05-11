package solver.ls;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;


public class Main {
	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage: java Main <file>");
			return;
		}

		String input = args[0];
		Path path = Paths.get(input);
		String filename = path.getFileName().toString();
		System.out.println("Instance: " + input);
		

		Timer watch = new Timer();
		watch.start();

		VRPInstance instance = new VRPInstance(input);
		// Solver solver = new Solver(100.0f, 0.01f, 0.99999f, 50000, 1500, instance, new TwoOpt(), new TopkRemoval());
		// Solver solver = new Solver(100.0f, 0.01f, 0.99999f, 500000, 1500, instance, new InsertionHeuristic[]{new TwoOpt(), new ThreeOpt(), new NearestNeighbor()}, new RandomRemoval());
		Solver solver = new Solver(10000.0f, 0.01f, 0.99999f, 100000, 1500, 1000, instance);
		Solution solution = solver.solve();

		watch.stop();

		// System.out.print(solution);

		// Write the solution.submissionFormat() to file

		
		// try {
		// 	Files.write(Paths.get("Solution.vrp.sol"), solution.fileOutputFormat().getBytes(StandardCharsets.UTF_8));
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }


		System.out.println("{\"Instance\": \"" + filename +
				"\", \"Time\": " + String.format("%.2f", watch.getTime()) +
				", \"Result\": " + ((double)Math.round(solution.evalSolution() * 100.0))/100.0 +
				", \"Solution\": \"" + solution.submissionFormat() + "\"}");
	}
}
