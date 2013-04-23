import java.io.IOException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.CSVLineRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CSVNLineInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CSVTextInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

public class SparseMatrixBuilder extends Configured implements Tool {

	public static class TestMapper extends
			Mapper<LongWritable, List<Text>, LongWritable, Text> {
		public void map(LongWritable key, List<Text> values, Context context)
				throws IOException, InterruptedException {
			System.out.println("TestMapper");
			System.out.println("key=" + key);
			int i = 0;
			String line = "key=" + key;
			for (Text val : values) {
				line += " val[" + (i++) + "] = " + val;
			}
			System.out.println(line);
			context.write(key, new Text(line));

		}
	}
	
	public static class Reduce extends
			Reducer<LongWritable, Text, LongWritable, Text> {

		public void reduce(LongWritable key, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			String str = "";
			for (Text val : values) {
				str+=val.toString();
			}
			context.write(key, new Text(str));
		}
	}

	

	@Override
	public int run(String[] args) throws Exception {
		Configuration conf = getConf();
		conf.set(CSVLineRecordReader.FORMAT_DELIMITER, "\"");
		conf.set(CSVLineRecordReader.FORMAT_SEPARATOR, ",");
		conf.setInt(CSVNLineInputFormat.LINES_PER_MAP, 40000);
		conf.setBoolean(CSVLineRecordReader.IS_ZIPFILE, false);

		
		Job job = new Job(conf, "matrix builder");
		job.setJarByClass(SparseMatrixBuilder.class);
		
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);

		job.setMapperClass(TestMapper.class);
		job.setReducerClass(Reduce.class);

		job.setInputFormatClass(CSVNLineInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[2]));

		job.waitForCompletion(true);
		return 0;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SparseMatrixBuilder sMB = new SparseMatrixBuilder();
		try {
			int res = ToolRunner.run(new Configuration(), sMB, args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
