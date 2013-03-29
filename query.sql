create table #answered (Id int, Label int)
	insert #answered
		SELECT Posts.Id AS Id, 1 AS Label
		FROM Posts
		WHERE Posts.PostTypeId=1 AND Posts.AcceptedAnswerId is not NULL

create table #unanswered (Id int, Label int)
	insert #unanswered
		SELECT Posts.Id AS Id, 0 AS Label
		FROM Posts 
		WHERE Posts.PostTypeId=1 AND Posts.AcceptedAnswerId is NULL

create table #labeled (Id int, Label int)
	insert #labeled
		SELECT * FROM #answered
		UNION
		SELECT * FROM #unanswered

SELECT #labeled.Id AS Id, 
	   #labeled.Label AS Label, 
	   Posts.Title AS Title, 
	   Posts.Score AS Score, 
	   Posts.Tags AS Tags,
	   Posts.CreationDate AS CreationDate,
	   Posts.ViewCount AS ViewCount,
	   Posts.LastEditDate AS LastEditDate,
	   Posts.LastActivityDate AS LastActivityDate,
	   Posts.Body AS Body,
	   Posts.AnswerCount AS AnswerCount,
	   Posts.CommentCount AS CommentCount,
	   Posts.FavoriteCount AS FavoriteCount, 
FROM #labeled JOIN Posts on #labeled.Id=Posts.Id