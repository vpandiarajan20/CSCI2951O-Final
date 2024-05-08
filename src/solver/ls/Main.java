package solver.ls;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import solver.ls.insertionHeuristics.TwoOpt;
import solver.ls.removalHeuristics.RandomRemoval;
import solver.ls.removalHeuristics.TopkRemoval;

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
		Solver solver = new Solver(100.0f, 0.01f, 0.99999f, 1000000, 1500, instance, new TwoOpt(), new TopkRemoval());
		Solution solution = solver.solve();

		watch.stop();

		System.out.print(solution);

		System.out.println("{\"Instance\": \"" + filename +
				"\", \"Time\": " + String.format("%.2f", watch.getTime()) +
				", \"Result\": \"--\"" +
				", \"Solution\": \"" + solution.submissionFormat() + "\"}");
	}
}
