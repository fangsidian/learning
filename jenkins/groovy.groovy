// this is a daily report template

import hudson.model.*
jenkins = Hudson.instance

// Get the list of failed jobs
activeJobs = hudson.model.Hudson.instance.items.findAll{job -> job.isBuildable()}
failedRuns = activeJobs.findAll{job -> job.lastBuild != null && (job.lastBuild.result == hudson.model.Result.FAILURE || job.lastBuild.result == hudson.model.Result.UNSTABLE)}

//remove unwanted jobs
failedRuns.removeAll{it.toString().contains("<the unwanted job name here>")}

// Do something with them - e.g. listing them
//failedRuns.each{run -> println(run.name)}

File lstFile = new File("/tmp/daily-report.html")

if (failedRuns.size()>0){
lstFile.withWriter{ out ->

  out.println "<html><h1>Jenkins Daily Report - The Following Jobs Are Failed/Unstable</h1>" + "<a href='<link to the jenkins page>'>Jenkins Build Monitor</a></p>"
  failedRuns.each {out.println it.name + "   ->   <a href="+jenkins.getRootUrl()+it.getUrl()+">Quick Link</a></p>"}
  out.println "</html>"
}
}else
{
  lstFile.withWriter{ out -> out.println"<html><h1>Daily Report - All Jobs Are Passed</h1>"}
}
