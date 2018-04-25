package nl.whizzkit.oracdc.writer;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.sql.ResultSet;

public class KafkaWriter implements IWritable {

    private final static String TOPIC = "my-example-topic";
    private final static Producer<Long, String> producer = KafkaUtils.createProducer();
    private long index;

    @Override
    public void write(ResultSet resultSet) throws Exception {
        producer.send(new ProducerRecord<>(TOPIC, index++, resultSet.getString("sql_redo"))).get();
    }

}
