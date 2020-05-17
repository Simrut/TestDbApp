package com.example.db

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.lo_persons.view.*

class PersonAdapter(mCtx : Context, val persons:ArrayList<Person>) : RecyclerView.Adapter<PersonAdapter.ViewHolder> (){

    val mCtx = mCtx

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val txtPersonName = itemView.txtPersonName
        val txtInfectionRisk = itemView.txtInfectionRisk
        val btnUpdate = itemView.btnUpdate
        val btnDelete = itemView.btnDelete
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonAdapter.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.lo_persons,parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return persons.size
    }

    override fun onBindViewHolder(holder: PersonAdapter.ViewHolder, position: Int) {
        val person:Person = persons[position]
        holder.txtPersonName.text = person.personName
        holder.txtInfectionRisk.text = person.infectionRisk.toString()
    }

}