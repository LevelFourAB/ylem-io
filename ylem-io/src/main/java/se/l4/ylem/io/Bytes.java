package se.l4.ylem.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import edu.umd.cs.findbugs.annotations.NonNull;
import se.l4.ylem.io.internal.BytesOverByteArray;
import se.l4.ylem.io.internal.InputStreamBytes;

/**
 * Representation of a stream of bytes that can be opened.
 */
public interface Bytes
{
	/**
	 * Open an {@link InputStream} for this instance.
	 *
	 * @return
	 *   stream that can be read from, the consumer must close the stream when
	 *   reading is done
	 * @throws IOException
	 *   if unable to open byte data as input stream
	 */
	@NonNull
	InputStream asInputStream()
		throws IOException;

	/**
	 * Convert this instance to a byte array.
	 *
	 * @return
	 *   byte array
	 * @throws IOException
	 *   if unable to read the the byte data
	 */
	@NonNull
	byte[] toByteArray()
		throws IOException;

	/**
	 * Stream this instance to the given consumer.
	 *
	 * @param consumer
	 * @throws IOException
	 */
	default void asChunks(@NonNull ByteArrayConsumer consumer)
		throws IOException
	{
		asChunks(4096, consumer);
	}

	/**
	 * Stream this instance to the given consumer with a specific chunk size.
	 *
	 * @param size
	 * @param consumer
	 * @throws IOException
	 */
	default void asChunks(int size, @NonNull ByteArrayConsumer consumer)
		throws IOException
	{
		Objects.requireNonNull(consumer);
		if(size <= 0) throw new IllegalArgumentException("size must be a positive number");

		try(InputStream in = asInputStream())
		{
			byte[] buf = new byte[size];
			int len;
			while((len = in.read(buf)) != -1)
			{
				consumer.consume(buf, 0, len);
			}
		}
	}

	/**
	 * Get an instance that represents no data.
	 *
	 * @return
	 *   instance of {@link Bytes} that contains no data
	 */
	@NonNull
	static Bytes empty()
	{
		return BytesOverByteArray.EMPTY;
	}

	/**
	 * Create an instance for the given array of bytes.
	 *
	 * @param byteArray
	 *   array containing the data that the instance should include. For
	 *   speed purposes the data is not copied by default, clone your array
	 *   if you intend to modify it later and want to keep the {@link Bytes}
	 *   instance unmodified
	 * @return
	 *   instance of {@link Bytes}
	 */
	@NonNull
	static Bytes create(@NonNull byte[] byteArray)
	{
		return create(byteArray, 0, byteArray.length);
	}

	/**
	 * Create an instance for the given array of bytes.
	 *
	 * @param byteArray
	 *   array containing the data that the instance should include. For
	 *   speed purposes the data is not copied by default, clone your array
	 *   if you intend to modify it later and want to keep the {@link Bytes}
	 *   instance unmodified
	 * @param offset
	 *   the offset to start reading byte data from
	 * @param length
	 *   the number of bytes to read from the array
	 * @return
	 *   instance of {@link Bytes}
	 */
	@NonNull
	static Bytes create(@NonNull byte[] byteArray, int offset, int length)
	{
		return new BytesOverByteArray(byteArray, offset, length);
	}

	/**
	 * Create an instance over the given input stream.
	 *
	 * @param supplier
	 *   supplier of {@link InputStream}, will be called every time the data
	 *   of the returned {@link Bytes} instance is opened
	 * @return
	 *   instance of {@link Bytes}
	 */
	@NonNull
	static Bytes create(@NonNull IOSupplier<InputStream> supplier)
	{
		return new InputStreamBytes(supplier);
	}

	/**
	 * Capture the content of an {@link InputStream} and store it in memory.
	 *
	 * @param stream
	 *   the stream to capture
	 * @return
	 *   instance of {@link Bytes}
	 */
	@NonNull
	static Bytes capture(@NonNull InputStream stream)
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		stream.transferTo(out);
		return create(out.toByteArray());
	}

	/**
	 * Capture some bytes by asking them to be written by the given output
	 * stream.
	 *
	 * @param stream
	 *   the stream to capture
	 * @return
	 *   instance of {@link Bytes}
	 */
	@NonNull
	static Bytes capture(@NonNull IOConsumer<OutputStream> creator)
		throws IOException
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		creator.accept(out);
		return create(out.toByteArray());
	}
}
