package com.nextdevv.automod.models;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Release {

    // Default constructor
    public Release() {
    }

    @SerializedName("url")
    private String url;

    @SerializedName("html_url")
    private String htmlUrl;

    @SerializedName("assets_url")
    private String assetsUrl;

    @SerializedName("upload_url")
    private String uploadUrl;

    @SerializedName("tarball_url")
    private String tarballUrl;

    @SerializedName("zipball_url")
    private String zipballUrl;

    @SerializedName("id")
    private Integer id;

    @SerializedName("node_id")
    private String nodeId;

    @SerializedName("tag_name")
    private String tagName;

    @SerializedName("target_commitish")
    private String targetCommitish;

    @SerializedName("name")
    private String name;

    @SerializedName("body")
    private String body;

    @SerializedName("draft")
    private Boolean draft;

    @SerializedName("prerelease")
    private Boolean prerelease;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("author")
    private SimpleUser author;

    @SerializedName("assets")
    private List<ReleaseAsset> assets;

    @SerializedName("body_html")
    private String bodyHtml;

    @SerializedName("body_text")
    private String bodyText;

    @SerializedName("mentions_count")
    private Integer mentionsCount;

    @SerializedName("discussion_url")
    private String discussionUrl;

    @SerializedName("reactions")
    private ReactionRollup reactions;

    @Getter
    @Setter
    public static class SimpleUser {

        public SimpleUser() {
        }

        @SerializedName("name")
        private String name;

        @SerializedName("email")
        private String email;

        @SerializedName("login")
        private String login;

        @SerializedName("id")
        private Integer id;

        @SerializedName("node_id")
        private String nodeId;

        @SerializedName("avatar_url")
        private String avatarUrl;

        @SerializedName("gravatar_id")
        private String gravatarId;

        @SerializedName("url")
        private String url;

        @SerializedName("html_url")
        private String htmlUrl;

        @SerializedName("followers_url")
        private String followersUrl;

        @SerializedName("following_url")
        private String followingUrl;

        @SerializedName("gists_url")
        private String gistsUrl;

        @SerializedName("starred_url")
        private String starredUrl;

        @SerializedName("subscriptions_url")
        private String subscriptionsUrl;

        @SerializedName("organizations_url")
        private String organizationsUrl;

        @SerializedName("repos_url")
        private String reposUrl;

        @SerializedName("events_url")
        private String eventsUrl;

        @SerializedName("received_events_url")
        private String receivedEventsUrl;

        @SerializedName("type")
        private String type;

        @SerializedName("site_admin")
        private Boolean siteAdmin;

        @SerializedName("starred_at")
        private String starredAt;

    }

    @Getter
    @Setter
    public static class ReleaseAsset {

        public ReleaseAsset() {
        }

        @SerializedName("url")
        private String url;

        @SerializedName("browser_download_url")
        private String browserDownloadUrl;

        @SerializedName("id")
        private Integer id;

        @SerializedName("node_id")
        private String nodeId;

        @SerializedName("name")
        private String name;

        @SerializedName("label")
        private String label;

        @SerializedName("state")
        private String state;

        @SerializedName("content_type")
        private String contentType;

        @SerializedName("size")
        private Integer size;

        @SerializedName("download_count")
        private Integer downloadCount;

        @SerializedName("created_at")
        private String createdAt;

        @SerializedName("updated_at")
        private String updatedAt;

        @SerializedName("uploader")
        private SimpleUser uploader;
    }

    @Getter
    @Setter
    public static class ReactionRollup {

        public ReactionRollup() {
        }

        @SerializedName("url")
        private String url;

        @SerializedName("total_count")
        private Integer totalCount;

        @SerializedName("+1")
        private Integer plusOne;

        @SerializedName("-1")
        private Integer minusOne;

        @SerializedName("laugh")
        private Integer laugh;

        @SerializedName("confused")
        private Integer confused;

        @SerializedName("heart")
        private Integer heart;

        @SerializedName("hooray")
        private Integer hooray;

        @SerializedName("eyes")
        private Integer eyes;

        @SerializedName("rocket")
        private Integer rocket;
    }
}
