import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.CSVLineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CSVTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class SparseMatrixBuilder {

	public class TestMapper extends Mapper<LongWritable, List<Text>, LongWritable, List<Text>> {
		public void map(LongWritable key, List<Text> values, Context context) throws IOException, InterruptedException {
			System.out.println("TestMapper");
			System.out.println("key=" + key);
			int i = 0;
			for (Text val : values)
				System.out.println("key=" + key + " val[" + (i++) + "] = " + val);
			// context.write(new LongWritable(1l), new Text("1"));

		}
	}

	public static class Reduce extends
			Reducer<Text, IntWritable, Text, IntWritable> {

		public void reduce(Text key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			int sum = 0;
			for (IntWritable val : values) {
				sum += val.get();
			}
			context.write(key, new IntWritable(sum));
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		conf.set(CSVLineRecordReader.FORMAT_DELIMITER, "\"");
		conf.set(CSVLineRecordReader.FORMAT_SEPARATOR, ",");

		Job job = new Job(conf, "matrix builder");

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		job.setMapperClass(TestMapper.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(CSVTextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));

		job.waitForCompletion(true);

	}
}
