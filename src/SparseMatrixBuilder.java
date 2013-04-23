import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.CSVLineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CSVTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

public class SparseMatrixBuilder {

	public static class TestMapper extends
			Mapper<LongWritable, List<Text>, LongWritable, List<Text>> {
		public void map(LongWritable key, List<Text> values, Context context)
				throws IOException, InterruptedException {
			System.out.println("TestMapper");
			System.out.println("key=" + key);
			int i = 0;
			String line = "key=" + key;
			for (Text val : values)
				line += " val[" + (i++) + "] = "
						+ val;
			System.out.println(line);
			context.write(key, values);

		}
	}

	public static class Reduce extends
			Reducer<LongWritable, List<Text>, LongWritable, List<Text>> {

		public void reduce(LongWritable key, Iterable<List<Text>> values,
				Context context) throws IOException, InterruptedException {
			List<Text> str = new LinkedList<Text>();
			for (List<Text> val : values) {
				str.addAll(val);
			}
			context.write(key, str);
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set(CSVLineRecordReader.FORMAT_DELIMITER, "\"");
		conf.set(CSVLineRecordReader.FORMAT_SEPARATOR, ",");
		
		Job job = new Job(conf, "matrix builder");
		job.setJarByClass(SparseMatrixBuilder.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(TestMapper.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(CSVTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		job.waitForCompletion(true);

	}
}
