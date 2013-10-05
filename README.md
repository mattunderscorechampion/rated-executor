
RatedExecutor
=============

Idea
====

A colleauge was working on a demo that polled the Twitter API. He had to solve the problem where he needed to ensure that the poll did not exceed the rate limits imposed on polling. This problem can easily be generalised into a generic problem, accessing a resource at no greater than a fixed rate. I didn't know of a generalised solution to the problem so I decided to implement one. A Runnable task executor that executes tasks no faster thana fixed rate but as soon as it can otherwise.

