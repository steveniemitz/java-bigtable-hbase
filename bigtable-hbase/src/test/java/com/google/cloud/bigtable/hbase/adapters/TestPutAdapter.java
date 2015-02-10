package com.google.cloud.bigtable.hbase.adapters;

import com.google.bigtable.v1.MutateRowRequest;
import com.google.bigtable.v1.Mutation;
import com.google.bigtable.v1.Mutation.MutationCase;
import com.google.bigtable.v1.Mutation.SetCell;
import com.google.cloud.bigtable.hbase.DataGenerationHelper;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Put;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.TimeUnit;

@RunWith(JUnit4.class)
public class TestPutAdapter {

  protected final PutAdapter adapter = new PutAdapter(new Configuration());
  protected final DataGenerationHelper dataHelper = new DataGenerationHelper();

  @Test
  public void testSingleCellIsConverted() {
    byte[] row = dataHelper.randomData("rk-");
    byte[] family = dataHelper.randomData("f");
    byte[] qualifier = dataHelper.randomData("qual");
    byte[] value = dataHelper.randomData("v1");
    long timestamp = 2L;

    Put hbasePut = new Put(row);
    hbasePut.add(family, qualifier, timestamp, value);

    MutateRowRequest.Builder rowMutationBuilder = adapter.adapt(hbasePut);
    Assert.assertArrayEquals(row, rowMutationBuilder.getRowKey().toByteArray());

    Assert.assertEquals(1, rowMutationBuilder.getMutationCount());
    Mutation mutation = rowMutationBuilder.getMutation(0);

    Assert.assertEquals(MutationCase.SET_CELL, mutation.getMutationCase());
    SetCell setCell = mutation.getSetCell();

    Assert.assertArrayEquals(family, setCell.getFamilyNameBytes().toByteArray());
    Assert.assertArrayEquals(qualifier, setCell.getColumnQualifier().toByteArray());
    Assert.assertEquals(
        TimeUnit.MILLISECONDS.toMicros(timestamp),
        setCell.getTimestampMicros());
    Assert.assertArrayEquals(value, setCell.getValue().toByteArray());
  }

  @Test
  public void testMultipleCellsInOneFamilyAreConverted() {
    byte[] row = dataHelper.randomData("rk-");
    byte[] family = dataHelper.randomData("f1");
    byte[] qualifier1 = dataHelper.randomData("qual1");
    byte[] qualifier2 = dataHelper.randomData("qual2");
    byte[] value1 = dataHelper.randomData("v1");
    byte[] value2 = dataHelper.randomData("v2");
    long timestamp1 = 1L;
    long timestamp2 = 2L;

    Put hbasePut = new Put(row);
    hbasePut.add(family, qualifier1, timestamp1, value1);
    hbasePut.add(family, qualifier2, timestamp2, value2);

    MutateRowRequest.Builder rowMutationBuilder = adapter.adapt(hbasePut);
    Assert.assertArrayEquals(row, rowMutationBuilder.getRowKey().toByteArray());

    Assert.assertEquals(2, rowMutationBuilder.getMutationCount());
    Mutation mutation = rowMutationBuilder.getMutation(0);

    Assert.assertEquals(MutationCase.SET_CELL, mutation.getMutationCase());
    SetCell setCell = mutation.getSetCell();
    Assert.assertArrayEquals(family, setCell.getFamilyNameBytes().toByteArray());
    Assert.assertArrayEquals(qualifier1, setCell.getColumnQualifier().toByteArray());
    Assert.assertEquals(
        TimeUnit.MILLISECONDS.toMicros(timestamp1),
        setCell.getTimestampMicros());
    Assert.assertArrayEquals(value1, setCell.getValue().toByteArray());

    Mutation mod2 = rowMutationBuilder.getMutation(1);
    SetCell setCell2 = mod2.getSetCell();
    Assert.assertArrayEquals(family, setCell2.getFamilyNameBytes().toByteArray());
    Assert.assertArrayEquals(qualifier2, setCell2.getColumnQualifier().toByteArray());
    Assert.assertEquals(
        TimeUnit.MILLISECONDS.toMicros(timestamp2),
        setCell2.getTimestampMicros());
    Assert.assertArrayEquals(value2, setCell2.getValue().toByteArray());
  }

  @Test
  public void testMultipleCellsInMultipleFamiliesAreConverted() {
    byte[] row = dataHelper.randomData("rk-");
    byte[] family1 = dataHelper.randomData("f1");
    byte[] family2 = dataHelper.randomData("f2");
    byte[] qualifier1 = dataHelper.randomData("qual1");
    byte[] qualifier2 = dataHelper.randomData("qual2");
    byte[] value1 = dataHelper.randomData("v1");
    byte[] value2 = dataHelper.randomData("v1");
    long timestamp1 = 1L;
    long timestamp2 = 2L;

    Put hbasePut = new Put(row);
    hbasePut.add(family1, qualifier1, timestamp1, value1);
    hbasePut.add(family2, qualifier2, timestamp2, value2);

    MutateRowRequest.Builder rowMutationBuilder = adapter.adapt(hbasePut);
    Assert.assertArrayEquals(row, rowMutationBuilder.getRowKey().toByteArray());

    Assert.assertEquals(2, rowMutationBuilder.getMutationCount());
    Mutation mutation1 = rowMutationBuilder.getMutation(0);

    Assert.assertEquals(MutationCase.SET_CELL, mutation1.getMutationCase());
    SetCell setCell = mutation1.getSetCell();
    Assert.assertArrayEquals(family1, setCell.getFamilyNameBytes().toByteArray());
    Assert.assertArrayEquals(qualifier1, setCell.getColumnQualifier().toByteArray());
    Assert.assertEquals(
        TimeUnit.MILLISECONDS.toMicros(timestamp1),
        setCell.getTimestampMicros());
    Assert.assertArrayEquals(value1, setCell.getValue().toByteArray());

    Mutation mutation2 = rowMutationBuilder.getMutation(1);
    SetCell setCell2 = mutation2.getSetCell();
    Assert.assertArrayEquals(family2, setCell2.getFamilyNameBytes().toByteArray());
    Assert.assertArrayEquals(qualifier2, setCell2.getColumnQualifier().toByteArray());
    Assert.assertEquals(
        TimeUnit.MILLISECONDS.toMicros(timestamp2),
        setCell2.getTimestampMicros());
    Assert.assertArrayEquals(value2, setCell2.getValue().toByteArray());
  }

  @Test
  public void testImplicitTimestampsAreUnset() {
    byte[] row = dataHelper.randomData("rk-");
    byte[] family1 = dataHelper.randomData("f1");
    byte[] qualifier1 = dataHelper.randomData("qual1");
    byte[] value1 = dataHelper.randomData("v1");

    Put hbasePut = new Put(row);
    hbasePut.add(family1, qualifier1, value1);

    MutateRowRequest.Builder rowMutationBuilder = adapter.adapt(hbasePut);
    Assert.assertArrayEquals(row, rowMutationBuilder.getRowKey().toByteArray());

    Assert.assertEquals(1, rowMutationBuilder.getMutationCount());
    Mutation mutation = rowMutationBuilder.getMutation(0);

    Assert.assertEquals(MutationCase.SET_CELL, mutation.getMutationCase());
    SetCell setCell = mutation.getSetCell();

    Assert.assertArrayEquals(family1, setCell.getFamilyNameBytes().toByteArray());
    Assert.assertArrayEquals(qualifier1, setCell.getColumnQualifier().toByteArray());
    Assert.assertEquals(0L, setCell.getTimestampMicros());
    Assert.assertArrayEquals(value1, setCell.getValue().toByteArray());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPut() {
    byte[] row = dataHelper.randomData("rk-");
    Put emptyPut = new Put(row);
    adapter.adapt(emptyPut);
  }
}
