package com.mytube.search.util;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.CountRequest;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.mytube.search.domain.ESSearchWord;
import com.mytube.search.domain.ESUser;
import com.mytube.search.domain.ESVideo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ESUtil {
    @Autowired
    private ElasticsearchClient client;

    public void upsertVideo(ESVideo esVideo) {
        if (esVideo == null || esVideo.getVid() == null) {
            return;
        }
        try {
            client.index(i -> i.index("video").id(esVideo.getVid().toString()).document(esVideo));
        } catch (IOException ignored) {}
    }

    public void deleteVideo(Integer vid) {
        if (vid == null) {
            return;
        }
        try {
            client.delete(d -> d.index("video").id(vid.toString()));
        } catch (IOException ignored) {}
    }

    public Long getVideoCount(String keyword, boolean onlyPass) {
        try {
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title", "tags").query(keyword)));
            Query query1 = Query.of(q -> q.constantScore(c -> c.filter(f -> f.term(t -> t.field("status").value(1)))));
            Query bool = Query.of(q -> q.bool(b -> b.must(query1).must(query)));
            CountRequest countRequest = onlyPass
                    ? new CountRequest.Builder().index("video").query(bool).build()
                    : new CountRequest.Builder().index("video").query(query).build();
            CountResponse countResponse = client.count(countRequest);
            return countResponse.count();
        } catch (IOException e) {
            return 0L;
        }
    }

    public List<Integer> searchVideosByKeyword(String keyword, Integer page, Integer size, boolean onlyPass) {
        try {
            List<Integer> list = new ArrayList<>();
            Query query = Query.of(q -> q.multiMatch(m -> m.fields("title", "tags").query(keyword)));
            Query query1 = Query.of(q -> q.constantScore(c -> c.filter(f -> f.term(t -> t.field("status").value(1)))));
            Query bool = Query.of(q -> q.bool(b -> b.must(query1).must(query)));
            SearchRequest searchRequest = onlyPass
                    ? new SearchRequest.Builder().index("video").query(bool).from((page - 1) * size).size(size).build()
                    : new SearchRequest.Builder().index("video").query(query).from((page - 1) * size).size(size).build();
            SearchResponse<ESVideo> searchResponse = client.search(searchRequest, ESVideo.class);
            for (Hit<ESVideo> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    list.add(hit.source().getVid());
                }
            }
            return list;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public Long getUserCount(String keyword) {
        try {
            Query query = Query.of(q -> q.simpleQueryString(s -> s.fields("nickname").query(keyword).defaultOperator(Operator.And)));
            CountRequest countRequest = new CountRequest.Builder().index("user").query(query).build();
            CountResponse countResponse = client.count(countRequest);
            return countResponse.count();
        } catch (IOException e) {
            return 0L;
        }
    }

    public List<Integer> searchUsersByKeyword(String keyword, Integer page, Integer size) {
        try {
            List<Integer> list = new ArrayList<>();
            Query query = Query.of(q -> q.simpleQueryString(s -> s.fields("nickname").query(keyword).defaultOperator(Operator.And)));
            SearchRequest searchRequest = new SearchRequest.Builder().index("user").query(query).from((page - 1) * size).size(size).build();
            SearchResponse<ESUser> searchResponse = client.search(searchRequest, ESUser.class);
            for (Hit<ESUser> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    list.add(hit.source().getUid());
                }
            }
            return list;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public void addSearchWord(String text) {
        try {
            ESSearchWord esSearchWord = new ESSearchWord(text);
            client.index(i -> i.index("search_word").document(esSearchWord));
        } catch (IOException ignored) {}
    }

    public List<String> getMatchingWord(String text) {
        try {
            List<String> list = new ArrayList<>();
            Query query = Query.of(q -> q.simpleQueryString(s -> s.fields("content").query(text).defaultOperator(Operator.And)));
            Query query1 = Query.of(q -> q.prefix(p -> p.field("content").value(text)));
            Query bool = Query.of(q -> q.bool(b -> b.should(query).should(query1)));
            SearchRequest searchRequest = new SearchRequest.Builder().index("search_word").query(bool).from(0).size(10).build();
            SearchResponse<ESSearchWord> searchResponse = client.search(searchRequest, ESSearchWord.class);
            for (Hit<ESSearchWord> hit : searchResponse.hits().hits()) {
                if (hit.source() != null) {
                    list.add(hit.source().getContent());
                }
            }
            return list;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }
}
