package com.gk.BookTracker.user;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Transient;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.CassandraType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import com.datastax.oss.driver.api.core.uuid.Uuids;

import org.springframework.data.cassandra.core.mapping.CassandraType.Name;
import org.springframework.data.cassandra.core.mapping.Column;

/*
 Model that represents the books read by a user.
 Helps with showing user's recent books in the dashboard page
 */

@Table(value = "books_by_user")
public class BooksByUser {

	@PrimaryKeyColumn(name = "user_id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
	private String id;
	
	@PrimaryKeyColumn(name = "book_id", ordinal = 1, type = PrimaryKeyType.CLUSTERED)
	@CassandraType(type = Name.TEXT)
	private String bookId;
	
	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING)
	@CassandraType(type = Name.TIMEUUID)
	private UUID timeUuid;
	
	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.ASCENDING)
	@CassandraType(type = Name.TEXT)
	private String readingStatus;
	
	@Column("book_name")
	@CassandraType(type = Name.TEXT)
	private String bookName;
	
	@Column("author_names")
	@CassandraType(type = Name.LIST, typeArguments = Name.TEXT)
	private List<String> authorNames;
	    
	@Column("cover_ids")
	@CassandraType(type = Name.LIST, typeArguments = Name.TEXT)
	private List<String> coverIds;
	
	@Column("rating")
	@CassandraType(type = Name.INT)
	private int rating;
	
	@Transient
	private String coverUrl;

	
	public BooksByUser() {
        this.timeUuid = Uuids.timeBased();
    }
	
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBookId() {
		return bookId;
	}

	public void setBookId(String bookId) {
		this.bookId = bookId;
	}

	public UUID getTimeUuid() {
		return timeUuid;
	}

	public void setTimeUuid(UUID timeUuid) {
		this.timeUuid = timeUuid;
	}

	public String getReadingStatus() {
		return readingStatus;
	}

	public void setReadingStatus(String readingStatus) {
		this.readingStatus = readingStatus;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public List<String> getCoverIds() {
		return coverIds;
	}

	public void setCoverIds(List<String> coverIds) {
		this.coverIds = coverIds;
	}

	public List<String> getAuthorNames() {
		return authorNames;
	}

	public void setAuthorNames(List<String> authorNames) {
		this.authorNames = authorNames;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public String getCoverUrl() {
		return coverUrl;
	}

	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}

	@Override
	public int hashCode() {
		return Objects.hash(bookId, id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BooksByUser other = (BooksByUser) obj;
		return Objects.equals(bookId, other.bookId) && Objects.equals(id, other.id);
	}
	
	
}
