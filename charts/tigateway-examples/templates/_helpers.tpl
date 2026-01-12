{{/*
Expand the name of the chart.
*/}}
{{- define "tigateway-examples.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Create a default fully qualified app name.
*/}}
{{- define "tigateway-examples.fullname" -}}
{{- if .Values.fullnameOverride }}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- $name := default .Chart.Name .Values.nameOverride }}
{{- if contains $name .Release.Name }}
{{- .Release.Name | trunc 63 | trimSuffix "-" }}
{{- else }}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" }}
{{- end }}
{{- end }}
{{- end }}

{{/*
Common labels
*/}}
{{- define "tigateway-examples.labels" -}}
helm.sh/chart: {{ include "tigateway-examples.chart" . }}
{{ include "tigateway-examples.selectorLabels" . }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- with .Values.global.labels }}
{{- toYaml . | nindent 0 }}
{{- end }}
{{- end }}

{{/*
Selector labels
*/}}
{{- define "tigateway-examples.selectorLabels" -}}
app.kubernetes.io/name: {{ include "tigateway-examples.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end }}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "tigateway-examples.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{- end }}

{{/*
Get namespace value
*/}}
{{- define "tigateway-examples.namespace" -}}
{{- if .Values.namespace }}
{{- .Values.namespace }}
{{- else if .Values.global.namespace }}
{{- .Values.global.namespace }}
{{- else }}
{{- "default" }}
{{- end }}
{{- end }}
