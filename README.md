
RatedExecutor
=============

Idea
====

A colleague was working on a demo that polled the Twitter API. He had to solve the problem where he needed to ensure that the poll did not exceed the rate limits imposed on polling. This problem can easily be generalised into a generic problem, accessing a resource at no greater than a fixed rate. I didn't know of a generalised solution to the problem so I decided to implement one. A Runnable task executor that executes tasks no faster than a fixed rate but as soon as it can otherwise.

Usage
=====

The RatedExecutor supports both Runnable and Callable tasks. Tasks can be submitted to be executed once, a fixed number of repetitions or an unbounded number of times. Callable tasks cannot be scheduled for an unbounded number of repetitions as this presents difficulty in storing the results.

Futures are returned that allow the result of the execution to be retrieved. The futures for tasks scheduled for a fixed number of repetitions allow the result of each repetition to be accessed.

The execute and submit methods schedule the task to be executed once. The difference between them is that execute does not return a future. The schedule methods can be used to repeatedly execute the same task. The schedule method is overloaded so that it can take an optional integer parameter that specifies the number of times to execute the task.

RatedExecutors can be constructed by using the static methods in the RatedExecutors class.

Testing
=======

Automated JUnit tests are included and have reasonable coverage. These tests tend towards integration rather than unit tests. They are subject to intermittent failure as a result of thread scheduling. They have assertions on the promptness and timing of the execution of tasks which make them unsuitable for measuring in terms of success and failure. I will refactor these to be assessed statistically. 

Continuous Integration
=======================

This project uses Travis CI, a free hosted CI integrated with GitHub. You can see the results for this project [here](https://travis-ci.org/mattunderscorechampion/rated-executor "rated-executor on travis-ci.org").
