package com.cloudwick.mapreduce.loganalytics.msgsizehits;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mapper calculates and emits the message size (rounded to 1024 bytes)
 */
public class ParseMsgSizeMapper extends Mapper<Object, Text, IntWritable, IntWritable> {

  public static final Pattern httplogPattern = Pattern.compile("^([\\d.]+) (\\S+) (\\S+) \\[(.*)\\] \"([^\\s]+)" +
      " (/[^\\s]*) HTTP/[^\\s]+\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"$");


  public static enum LOG_PROCESSOR_COUNTER {
    MALFORMED_RECORDS,
    PROCESSED_RECORDS,
  }

  private final static IntWritable one = new IntWritable(1);

  public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
    Matcher matcher = httplogPattern.matcher(value.toString());
    if (matcher.matches()) {
      context.getCounter(LOG_PROCESSOR_COUNTER.PROCESSED_RECORDS).increment(1);
      int size = Integer.parseInt(matcher.group(7));
      context.write(new IntWritable(size / 1024), one);
    } else {
      context.getCounter(LOG_PROCESSOR_COUNTER.MALFORMED_RECORDS).increment(1);
    }
  }
}
