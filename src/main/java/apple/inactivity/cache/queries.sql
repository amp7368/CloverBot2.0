-- list of guilds
select *
from guild;

-- list of channels
select guild.guild_name,q.*
from guild
         inner join (
    select channel.*, guild_id
    from channel
             inner join (
        select distinct channel_id, guild_id
        from message
    ) as my_channels on my_channels.channel_id = channel.channel_id) q on q.guild_id = guild.guild_id
order by guild_name;

-- list of channels for a particular guild
select channel.*
from channel
         inner join (
    select distinct channel_id
    from message
    where guild_id = 603039156892860417 -- the particular guild
) as my_channels on my_channels.channel_id = channel.channel_id;

-- list of messages from a particular channel
select author.author_name, m.content, m.time_stamp
from author
         inner join
     (
         select * from message where channel_id = 850598624299450370 order by time_stamp
     ) m on m.author_id = author.author_id

