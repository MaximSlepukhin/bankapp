{{- define "zipkin.name" -}}
zipkin
{{- end -}}

{{- define "zipkin.fullname" -}}
{{ include "zipkin.name" . }}
{{- end -}}
